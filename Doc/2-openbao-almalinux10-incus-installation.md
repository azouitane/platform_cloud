# OpenBao 2.6.4 sur AlmaLinux 10.2 dans Incus

Guide d'installation et de configuration pour le laboratoire **platform-cloud**.

> **Environnement observé :** Incus **CONTAINER** `openbao-alma`, AlmaLinux 10.2 x86_64, IP `10.0.23.29`, OpenBao `2.6.4-1.el10_2`. Le service RPM lance `bao server -config=/etc/openbao.d`. Ce document utilise ces valeurs d'exemple ; adaptez l'adresse si elle change.
>
> **Sécurité :** guide pour laboratoire / staging. Un nœud Raft unique ne fournit pas de haute disponibilité. Ne stockez jamais les clés d'unseal ou les tokens dans Git, dans ce document, ou dans des captures de terminal.

## 1. Architecture

```text
Fedora (hôte Incus / Spring Boot)
  ├── cluster-service (PostgreSQL: secretReference seulement)
  ├── secret-service (gestion des secrets)
  └── infrastructure-service (lecture des secrets)
       │ HTTPS 8200, vérification du certificat
       ▼
Incus: openbao-alma [CONTAINER] — 10.0.23.29
  └── AlmaLinux 10.2
       └── OpenBao 2.6.4 (systemd)
            ├── TLS
            ├── Raft: /var/lib/openbao/raft
            └── KV v2: secret/proxmox/*
```

## 2. Création du conteneur et accès

Sur Fedora, si le conteneur n'existe pas encore :

```bash
incus launch images:almalinux/10 openbao-alma
incus list
incus exec openbao-alma -- bash
```

**Attention :** ceci crée un conteneur Incus, pas une VM. Dans l'installation actuelle, le conteneur existe déjà : **ne relancez pas** `incus launch`.

À l'intérieur :

```bash
cat /etc/os-release
uname -m
ip -br addr
```

## 3. Installation via EPEL

```bash
dnf update -y
dnf install -y epel-release
dnf install -y openbao openssl ca-certificates
bao version
```

Le dépôt `rpm.releases.hashicorp.com` correspond à HashiCorp, **pas** à OpenBao. S'il a été ajouté par erreur :

```bash
ls /etc/yum.repos.d/
# Supprimer uniquement si ce fichier a bien été ajouté pour cette installation :
rm -f /etc/yum.repos.d/hashicorp.repo
dnf clean all
```

Vérifier le service installé :

```bash
systemctl cat openbao
id openbao
```

Sur cette machine, le service charge **tous les fichiers `.hcl` / `.json` de `/etc/openbao.d`** :

```ini
ExecStart=/usr/bin/bao server -config=/etc/openbao.d
```

## 4. Vérifications AVANT migration du stockage

La configuration RPM initiale a été observée avec `127.0.0.1:8200`, TLS désactivé et stockage `file`. **Ne remplacez pas un backend initialisé par Raft sans migration ni sauvegarde.**

```bash
export BAO_ADDR=http://127.0.0.1:8200
unset BAO_CACERT
bao status
find /etc/openbao.d -maxdepth 1 -type f -print
```

- Si `Initialized: false` et aucune donnée utile : vous pouvez configurer un backend Raft neuf.
- Si `Initialized: true` : **arrêtez-vous ici** et préparez une migration `bao operator migrate` et une sauvegarde validée. Les commandes de réinitialisation ci-dessous ne sont pas destinées à un serveur déjà initialisé.
- Si le serveur a déjà été reconfiguré en TLS, utilisez `https://10.0.23.29:8200` et le certificat de confiance à la place de l'URL HTTP locale.

## 5. Sauvegarde de configuration et répertoires

```bash
systemctl stop openbao
cp -a /etc/openbao.d "/root/openbao.d.backup.$(date +%Y%m%d-%H%M%S)"
find /etc/openbao.d -maxdepth 1 -type f \( -name '*.hcl' -o -name '*.json' \) -print
mkdir -p /etc/openbao.d/tls /var/lib/openbao/raft
chown -R openbao:openbao /var/lib/openbao/raft
chmod 700 /var/lib/openbao/raft
```

**Important :** déplacez hors de `/etc/openbao.d` les anciennes configurations `.hcl` / `.json` contenant un autre `listener` ou `storage`, après inspection. Une sauvegarde dans un sous-dossier séparé est préférable ; ne conservez pas de configuration concurrente dans le dossier actif.

## 6. TLS pour le laboratoire

Certificat auto-signé avec SAN IP (laboratoire seulement) :

```bash
openssl req -x509 -newkey rsa:3072 -sha256 -nodes -days 365 \
  -keyout /etc/openbao.d/tls/server.key \
  -out /etc/openbao.d/tls/server.crt \
  -subj '/CN=openbao.lab' \
  -addext 'subjectAltName=DNS:openbao.lab,IP:10.0.23.29' \
  -addext 'basicConstraints=critical,CA:TRUE' \
  -addext 'keyUsage=critical,digitalSignature,keyEncipherment,keyCertSign' \
  -addext 'extendedKeyUsage=serverAuth'

chown root:openbao /etc/openbao.d/tls/server.key
chmod 640 /etc/openbao.d/tls/server.key
chmod 644 /etc/openbao.d/tls/server.crt
```

Pour la production, préférez un certificat serveur signé par une CA interne fiable, un DNS stable et une rotation documentée. Ne désactivez pas la vérification TLS.

## 7. Configuration HCL — Raft + HTTPS

Créer `/etc/openbao.d/openbao.hcl` :

```hcl
ui = true

api_addr     = "https://10.0.23.29:8200"
cluster_addr = "https://10.0.23.29:8201"

storage "raft" {
  path    = "/var/lib/openbao/raft"
  node_id = "openbao-alma"
}

listener "tcp" {
  address         = "0.0.0.0:8200"
  cluster_address = "0.0.0.0:8201"
  tls_cert_file   = "/etc/openbao.d/tls/server.crt"
  tls_key_file    = "/etc/openbao.d/tls/server.key"
}

disable_mlock = true
```

`disable_mlock = true` est ici un compromis pour le conteneur de laboratoire. Vérifiez la politique de protection mémoire avant une mise en production. Le port `8201` sert aux communications entre nœuds ; ne l'exposez pas publiquement.

Contrôler les permissions et SELinux :

```bash
getenforce
restorecon -Rv /etc/openbao.d /var/lib/openbao
```

Si SELinux bloque l'accès, examinez `journalctl` et les journaux d'audit ; ne désactivez pas SELinux pour contourner le problème.

## 8. Démarrer et vérifier systemd

```bash
systemctl enable --now openbao
systemctl restart openbao
systemctl status openbao --no-pager
journalctl -u openbao -n 50 --no-pager
```

Recherchez le **dernier démarrage** :

```text
Listener 1: tcp (addr: "0.0.0.0:8200", ... tls: "enabled")
Storage: raft
```

Une ancienne ligne `127.0.0.1` peut apparaître dans l'historique des journaux : ce n'est pas la configuration courante si une ligne plus récente indique `0.0.0.0` et TLS actif.

## 9. Vérifier l'API HTTPS

Dans le conteneur :

```bash
export BAO_ADDR='https://10.0.23.29:8200'
export BAO_CACERT='/etc/openbao.d/tls/server.crt'
bao status
```

Sur un backend Raft neuf, `Initialized: false`, `Sealed: true`, `Storage Type: raft` sont attendus. Un code retour non nul est possible lorsque le serveur est scellé.

Depuis Fedora :

```bash
incus file pull openbao-alma/etc/openbao.d/tls/server.crt ./openbao-ca.crt
curl --cacert ./openbao-ca.crt -i https://10.0.23.29:8200/v1/sys/health
```

Le code HTTP `501` (non initialisé) ou `503` (scellé) peut indiquer que **TLS et le réseau fonctionnent**, même si le serveur n'est pas encore opérationnel.

Restreignez le pare-feu au réseau d'administration et aux services autorisés. Vérifiez aussi que l'IP Incus est accessible depuis l'environnement réel des microservices.

## 10. Initialisation et unseal — une seule fois

**Uniquement si `Initialized: false` et backend Raft neuf :**

```bash
bao operator init -key-shares=3 -key-threshold=2
```

Conservez les **trois clés unseal** et le **root token initial** dans un coffre sûr, séparé du conteneur et de son stockage. Ne les publiez pas dans les logs ou le dépôt Git.

```bash
bao operator unseal   # première clé
bao operator unseal   # deuxième clé, différente
bao status
bao login             # root token initial, pour la configuration initiale uniquement
```

Résultat attendu :

```text
Initialized     true
Sealed          false
Storage Type   raft
```

Avec un seal Shamir manuel, un redémarrage peut nécessiter un nouvel unseal. Évitez de distribuer le root token aux applications.

## 11. KV v2 et secret Proxmox

Après authentification administrative :

```bash
bao secrets list -detailed
```

Si le chemin `secret/` n'existe pas :

```bash
bao secrets enable -path=secret -version=2 kv
```

Si `secret/` existe déjà, vérifiez qu'il s'agit bien de KV v2 ; ne tentez pas de le recréer.

Pour éviter de placer les secrets réels dans l'historique shell, utilisez un fichier JSON temporaire créé dans un répertoire privé ou une intégration sécurisée. Exemple **avec valeurs fictives uniquement** :

```bash
bao kv put -mount=secret proxmox/cluster-01 \
  token_id='cloud@pve!infrastructure' \
  token_secret='DEMO_ONLY_NOT_A_REAL_SECRET'

bao kv get -mount=secret proxmox/cluster-01
```

Évitez `bao kv get` en clair sur un terminal partagé avec de vrais secrets. En base PostgreSQL, enregistrez une référence comme `secret/proxmox/cluster-01`, **jamais** le token secret.

## 12. Policies pour les microservices

### infrastructure-service : lecture seule

Fichier `infrastructure-service-policy.hcl` :

```hcl
path "secret/data/proxmox/*" {
  capabilities = ["read"]
}
```

### secret-service : gestion des secrets

Fichier `secret-service-policy.hcl` :

```hcl
path "secret/data/proxmox/*" {
  capabilities = ["create", "read", "update", "delete"]
}

path "secret/metadata/proxmox/*" {
  capabilities = ["read", "list", "delete"]
}
```

Charger les policies :

```bash
bao policy write infrastructure-service infrastructure-service-policy.hcl
bao policy write secret-service secret-service-policy.hcl
bao policy list
```

Pour des tests de courte durée seulement :

```bash
bao token create -policy=infrastructure-service -ttl=8h
bao token create -policy=secret-service -ttl=8h
```

En production, utilisez AppRole ou Kubernetes Auth, des tokens à durée limitée, leur renouvellement et une séparation stricte des rôles.

## 13. Intégration Spring Boot

Dépendance Maven :

```xml
<dependency>
  <groupId>org.springframework.vault</groupId>
  <artifactId>spring-vault-core</artifactId>
</dependency>
```

Gérez la version via un BOM compatible avec votre version Spring Boot / Spring Vault. Configuration indicative :

```yaml
openbao:
  uri: ${BAO_ADDR:https://10.0.23.29:8200}
  token: ${INFRASTRUCTURE_BAO_TOKEN}
  kv-mount: secret
```

> Ces propriétés `openbao.*` sont **personnalisées** : elles ne configurent pas automatiquement `VaultTemplate`. Il faut créer les beans Spring Vault (endpoint, authentification, TLS/SSL) ou utiliser les propriétés `spring.cloud.vault.*` si Spring Cloud Vault est installé. Importez le certificat de confiance dans un truststore Java adapté. Ne mettez aucun token en dur dans `application.yml`.

Workflow conseillé : `cluster-service` conserve `secretReference`, `secret-service` écrit/rotate les secrets, `infrastructure-service` lit les credentials nécessaires à l'appel Proxmox avec une policy en lecture seule.

## 14. Maintenance et diagnostics

```bash
systemctl is-enabled openbao
systemctl is-active openbao
journalctl -u openbao -n 100 --no-pager
ss -lntp | grep 8200
bao status
```

Erreurs courantes :

| Symptôme | Cause probable | Vérification |
|---|---|---|
| `connection refused` sur `10.0.23.29:8200` | écoute uniquement sur loopback ou service arrêté | `ss -lntp`, `journalctl` |
| `http: server gave HTTP response to HTTPS client` | TLS désactivé côté serveur | configuration `listener` |
| `x509: certificate signed by unknown authority` | CA non approuvée | `BAO_CACERT`, truststore Java |
| `permission denied` | droits Unix / SELinux | `id openbao`, `ls -lZ`, audit |
| `Storage: file` | ancien fichier de configuration encore actif | `find /etc/openbao.d` |
| `Sealed: true` après reboot | unseal requis | `bao operator unseal` |

### Sauvegarde

Sur un nœud Raft initialisé et déverrouillé, planifiez des snapshots chiffrés, conservés **hors du conteneur** :

```bash
bao operator raft snapshot save /chemin/securise/openbao-raft.snap
```

Un snapshot contient des données sensibles : restreignez ses permissions, protégez son transport, testez la restauration dans un environnement isolé et sauvegardez séparément les éléments nécessaires à l'unseal. Une copie du seul fichier HCL n'est **pas** une sauvegarde des secrets.

## 15. Checklist de validation

- [ ] `bao version` indique OpenBao 2.6.4 sur AlmaLinux 10.2.
- [ ] `systemctl status openbao` indique `active (running)`.
- [ ] Le dernier démarrage indique `0.0.0.0:8200` et `tls: enabled`.
- [ ] Le stockage actif est `raft`, et non `file`.
- [ ] `bao status` indique `Initialized: true`, `Sealed: false`.
- [ ] Fedora vérifie le certificat et accède à l'API HTTPS.
- [ ] KV v2 `secret/` est activé.
- [ ] Les policies sont séparées par microservice.
- [ ] Aucun secret ou root token n'est présent dans Git.
- [ ] Les sauvegardes et procédures de récupération sont documentées.

## Sources officielles

- Installation : https://openbao.org/docs/install/
- Configuration HCL : https://openbao.org/docs/configuration/
- Stockage Raft : https://openbao.org/docs/configuration/storage/raft/
- Initialisation : https://openbao.org/docs/commands/operator/init/
- Stockage : https://openbao.org/docs/configuration/storage/
