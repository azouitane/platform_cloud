# Installation et configuration — Proxmox VE Cluster `cloud-cluster`

> **Environnement :** Fedora (hôte KVM/libvirt), deux machines virtuelles Proxmox VE (`pve01`, `pve02`).  
> **État confirmé le 24/10/2026 :** cluster `cloud-cluster` fonctionnel, 2 nœuds membres, `Quorate: Yes`, configuration version 2.  
> **Objet :** documenter l'installation, la configuration réseau, la création du cluster, les problèmes rencontrés et les contrôles d'exploitation.

## 1. Architecture

```text
                    Fedora — KVM / libvirt
                             |
              +--------------+---------------+
              |                              |
       Réseau Management               Réseau Corosync
       192.168.122.0/24                10.10.10.0/24
              |                              |
    +---------+---------+          +---------+---------+
    |                   |          |                   |
  pve01               pve02      pve01               pve02
  vmbr0               vmbr0      vmbr1               vmbr1
  .201                .202       .101                .102
    |                   |          |                   |
    +--------- Proxmox VE cluster cloud-cluster ------+
```

| Nœud | Interface | IP/CIDR | Rôle |
|---|---|---|---|
| pve01 | `nic0` → `vmbr0` | `192.168.122.201/24` | Management, HTTPS API :8006 |
| pve01 | `nic1` → `vmbr1` | `10.10.10.101/24` | Corosync |
| pve02 | `nic0` → `vmbr0` | `192.168.122.202/24` | Management, HTTPS API :8006 |
| pve02 | `nic1` → `vmbr1` | `10.10.10.102/24` | Corosync |

**Important :** les adresses ci-dessus correspondent aux dernières sorties `ip a` partagées, et remplacent les anciennes adresses envisagées (`192.168.122.200`, `192.168.122.203`, `10.10.10.1`, etc.). Le réseau Corosync doit être relié entre les deux VM sur un segment libvirt distinct. L'isolation virtuelle ne fournit pas une redondance physique : l'hôte Fedora reste un point de défaillance unique.

## 2. Préparer l'hôte Fedora

### 2.1 Installer et démarrer libvirt (si nécessaire)

```bash
sudo dnf install -y @virtualization virt-install virt-manager
sudo systemctl enable --now libvirtd
sudo virsh -c qemu:///system list --all
```

> Selon la version de Fedora, libvirt peut utiliser des démons modulaires. Si `libvirtd` n'existe pas, vérifier les services `virtqemud` et `virtnetworkd` plutôt que de supposer que l'installation a échoué.

### 2.2 Préparer les VM Proxmox

Créer deux VM x86_64 dans libvirt, démarrées sur l'ISO officielle de Proxmox VE, avec virtualisation imbriquée activée si des VM KVM doivent fonctionner à l'intérieur de Proxmox. Prévoir **deux interfaces réseau par VM** : une Management et une Corosync. Le détail de la création initiale des VM dépend des disques, de l'ISO et des noms des domaines libvirt ; il n'est pas reconstitué ici comme une commande prétendument déjà exécutée.

Vérifier les domaines et interfaces :

```bash
sudo virsh -c qemu:///system list --all
sudo virsh -c qemu:///system domiflist pve01
sudo virsh -c qemu:///system domiflist pve02
```

Adresses MAC des interfaces Corosync observées :

- `pve01` `nic1` : `52:54:00:35:ac:f2`
- `pve02` `nic1` : `52:54:00:fc:58:bc`

### 2.3 Créer un réseau libvirt isolé pour Corosync (si absent)

```bash
cat > /tmp/corosync-net.xml <<'XML'
<network>
  <name>corosync-net</name>
  <bridge name='virbr-corosync' stp='off' delay='0'/>
</network>
XML

sudo virsh -c qemu:///system net-list --all
# Exécuter net-define uniquement si corosync-net n'existe pas :
sudo virsh -c qemu:///system net-define /tmp/corosync-net.xml
sudo virsh -c qemu:///system net-start corosync-net
sudo virsh -c qemu:///system net-autostart corosync-net
```

Ce réseau libvirt isolé n'a ni DHCP ni passerelle. **`virsh net-port-create corosync-net` n'est pas la commande de création d'un réseau** : elle nécessite un fichier XML de port (`--file`).

Pour rattacher les cartes `nic1` existantes, consulter leur MAC puis modifier les définitions persistantes :

```bash
sudo virsh -c qemu:///system edit pve01
sudo virsh -c qemu:///system edit pve02
```

Dans **l'interface dont la MAC correspond à `nic1`**, utiliser `<source network='corosync-net'/>`. Conserver la MAC et le modèle, ne pas modifier `nic0`. Appliquer le changement lors d'un arrêt/redémarrage contrôlé des VM. Vérifier ensuite `domiflist`.

## 3. Installer Proxmox VE sur les deux VM

1. Monter l'ISO officielle Proxmox VE dans chaque domaine libvirt.
2. Démarrer chaque VM et suivre l'installateur Proxmox VE.
3. Définir le hostname `pve01` puis `pve02` et les adresses Management ci-dessus.
4. Configurer la passerelle du réseau Management suivant le réseau libvirt réel (ne pas inventer de gateway).
5. Après l'installation, ouvrir `https://192.168.122.201:8006` et `https://192.168.122.202:8006` depuis un poste qui peut atteindre ce réseau.
6. Vérifier les versions et la synchronisation de l'heure avant le clustering.

```bash
pveversion -v
hostnamectl
ip -br addr
timedatectl
```

Les VM de ce lab sont **déjà installées** ; cette section sert à reproduire l'environnement, et non à affirmer une procédure d'installation historique non observée.

## 4. Configurer le réseau sur Proxmox

Fichier : `/etc/network/interfaces`. Exemples à adapter à la passerelle Management réelle. **Ne pas remplacer à l'aveugle un fichier fonctionnel** ; sauvegarder avant modification et disposer d'un accès console libvirt.

### pve01

```text
auto lo
iface lo inet loopback

iface nic0 inet manual
iface nic1 inet manual

auto vmbr0
iface vmbr0 inet static
    address 192.168.122.201/24
    gateway <PASSERELLE_MANAGEMENT_REELLE>
    bridge-ports nic0
    bridge-stp off
    bridge-fd 0

auto vmbr1
iface vmbr1 inet static
    address 10.10.10.101/24
    bridge-ports nic1
    bridge-stp off
    bridge-fd 0
```

### pve02

```text
auto lo
iface lo inet loopback

iface nic0 inet manual
iface nic1 inet manual

auto vmbr0
iface vmbr0 inet static
    address 192.168.122.202/24
    gateway <PASSERELLE_MANAGEMENT_REELLE>
    bridge-ports nic0
    bridge-stp off
    bridge-fd 0

auto vmbr1
iface vmbr1 inet static
    address 10.10.10.102/24
    bridge-ports nic1
    bridge-stp off
    bridge-fd 0
```

La ligne `gateway` est **un emplacement à remplacer**, pas une ligne exécutable telle quelle. Si la configuration existante fournit déjà la route par défaut, la conserver ; ne pas ajouter une seconde gateway. Pas de gateway sur `vmbr1`.

Après vérification et sauvegarde :

```bash
cp /etc/network/interfaces /root/interfaces.backup
# Modifier ensuite le fichier si nécessaire
ifreload -a
ip -br addr
ip route
```

## 5. Résolution des noms

Sur **les deux nœuds**, dans `/etc/hosts`, conserver les lignes IPv6 existantes et utiliser :

```text
127.0.0.1 localhost.localdomain localhost
192.168.122.201 pve01.local pve01
192.168.122.202 pve02.local pve02
10.10.10.101 pve01-corosync
10.10.10.102 pve02-corosync
```

Ne pas reproduire l'ancienne faute `0.10.10.101` et ne pas attribuer `pve01.local` aux deux interfaces.

```bash
getent hosts pve01.local
getent hosts pve02.local
getent hosts pve01-corosync
getent hosts pve02-corosync
```

## 6. Tests de connectivité

Sur `pve01` :

```bash
ping -c 4 -I vmbr0 192.168.122.202
ping -c 4 -I vmbr1 10.10.10.102
ip route get 10.10.10.102
```

Sur `pve02` :

```bash
ping -c 4 -I vmbr0 192.168.122.201
ping -c 4 -I vmbr1 10.10.10.101
ip route get 10.10.10.101
```

**Résultat réellement observé** sur `pve02` vers `10.10.10.101` : 4 paquets reçus sur 4, perte 0 %, RTT moyen 0,633 ms.

## 7. Création du cluster (à exécuter une seule fois)

> **Déjà réalisé dans ce lab : ne pas relancer ces commandes sur les nœuds actuels.** Avant l'ajout d'un nœud, celui-ci ne doit pas avoir de VM/LXC ou de configuration cluster à préserver : l'adhésion remplace la configuration locale `/etc/pve`.

Sur `pve01` (uniquement pour un nouveau cluster) :

```bash
pvecm create cloud-cluster --link0 10.10.10.101
pvecm status
```

Sur `pve02` (uniquement pour rejoindre le cluster) :

```bash
pvecm add pve01.local --link0 10.10.10.102
```

Le certificat TLS doit correspondre au hostname utilisé et son empreinte doit être vérifiée avant de saisir **`yes`** (pas `y`).

Vérification :

```bash
pvecm status
pvecm nodes
cat /etc/pve/corosync.conf
systemctl status corosync --no-pager
```

### État final effectivement confirmé

```text
Cluster name: cloud-cluster
Config Version: 2
Transport: knet
Nodes: 2
Expected votes: 2
Total votes: 2
Quorum: 2
Quorate: Yes

Nodeid  Votes  Name
1       1      pve01 (local)
2       1      pve02

Corosync: 10.10.10.101 et 10.10.10.102
```

## 8. Dépannage rencontré

### `cluster config '/etc/pve/corosync.conf' already exists`

Le cluster est déjà initialisé sur `pve01`. **Ne pas répéter `pvecm create`**. Contrôler `pvecm status` et `cat /etc/pve/corosync.conf`.

### `Fingerprint not verified, abort!`

Le prompt attend `yes` en entier, après comparaison de l'empreinte SHA256 :

```bash
openssl x509 -in /etc/pve/local/pve-ssl.pem -noout -fingerprint -sha256
```

### `500 Can't connect ... (hostname verification failed)`

Le certificat de `pve01` contenait `DNS:pve01`, `DNS:pve01.local`, mais l'ancienne IP `192.168.122.175`, et non `192.168.122.201`. L'ajout via l'IP échouait sur la vérification du nom TLS. Utiliser le hostname qui figure dans le certificat, après validation de `/etc/hosts` :

```bash
getent hosts pve01.local
openssl x509 -in /etc/pve/local/pve-ssl.pem -noout -subject -ext subjectAltName
pvecm add pve01.local --link0 10.10.10.102
```

Si une régénération du certificat est nécessaire après correction du hostname, elle doit être planifiée car elle peut interrompre brièvement l'accès HTTPS :

```bash
pvecm updatecerts --force
systemctl restart pveproxy
```

Ne pas désactiver la validation TLS pour contourner le problème.

## 9. Contrôler les ressources et le stockage

```bash
pvesh get /nodes
pvesh get /cluster/resources
pvesh get /cluster/resources --type vm
pvesm status
lvs -a
vgs
lsblk -o NAME,SIZE,FSTYPE,MOUNTPOINTS
qm list
pct list
```

**Dernier état observé :**

| Ressource | pve01 | pve02 |
|---|---:|---:|
| Statut | online | online |
| CPU virtuels | 2 | 2 |
| RAM totale | 1,92 GiB | 1,92 GiB |
| RAM utilisée | ~1,52 GiB | ~1,46 GiB |
| Stockage `local` | présent | présent |
| Stockage `local-lvm` | présent | présent |

`local` propose ISO, templates LXC, backups et imports ; `local-lvm` propose disques VM et rootfs LXC. L'affichage `0.00 B` dans la colonne d'utilisation ne prouve pas une absence de capacité. Examiner `pvesm status` et `lvs` avant de créer des invités.

**Capacité :** avec moins de 0,5 GiB de RAM non utilisée par nœud au moment de la mesure, ne pas lancer de VM lourdes sans augmenter les ressources du lab.

## 10. API Proxmox pour l'application Cloud Infrastructure

| Fonction | Endpoint HTTP |
|---|---|
| Nœuds | `GET /api2/json/nodes` |
| Ressources | `GET /api2/json/cluster/resources` |
| Statut d'un nœud | `GET /api2/json/nodes/pve01/status` |
| VM d'un nœud | `GET /api2/json/nodes/pve01/qemu` |
| LXC d'un nœud | `GET /api2/json/nodes/pve01/lxc` |
| Storage d'un nœud | `GET /api2/json/nodes/pve01/storage` |
| VMID libre | `GET /api2/json/cluster/nextid` |

Équivalents CLI locaux :

```bash
pvesh get /nodes
pvesh get /cluster/resources
pvesh get /cluster/nextid
```

Pour Spring Boot, utiliser un **API token à privilèges minimaux**, conserver les secrets dans un gestionnaire de secrets, vérifier les certificats TLS et suivre les tâches Proxmox de manière asynchrone. Ne pas exposer le token au frontend.

## 11. Quorum, disponibilité et limites

Un cluster à deux nœuds peut être **quorate** tant que les deux membres sont disponibles, mais il risque de perdre le quorum si un seul nœud tombe. Pour un lab HA, ajouter un **QDevice indépendant** (troisième vote) et étudier le stockage partagé, les backups et la capacité de reprise. Un QDevice hébergé sur le même Fedora ne protège pas contre une panne de cet hôte physique. Le cluster n'est **pas automatiquement** une infrastructure HA de production.

## 12. Maintenance — commandes utiles

```bash
pvecm status
pvecm nodes
cat /etc/pve/corosync.conf
systemctl is-active corosync pve-cluster pveproxy
journalctl -u corosync -n 50 --no-pager
pvesh get /nodes
pvesm status
```

## 13. Documentation officielle

- [Proxmox VE Administration Guide](https://pve.proxmox.com/pve-docs/pve-admin-guide.pdf)
- [Network Configuration — Proxmox VE](https://pve.proxmox.com/wiki/Network_Configuration)
- [Proxmox Cluster File System](https://pve.proxmox.com/pve-docs/chapter-pmxcfs.html)
- [Migration vers Proxmox VE — concepts de cluster](https://pve.proxmox.com/wiki/Migrate_to_Proxmox_VE)

---

**Document :** `proxmox.md` — configuration documentée à partir des sorties de commandes fournies, avec procédures reproductibles et précautions de sécurité.
