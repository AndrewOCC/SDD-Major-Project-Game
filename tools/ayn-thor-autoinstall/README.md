# AYN Thor auto-install

Push a commit → CI builds + tests → CI pings ntfy.sh → the Thor pulls the
artifact and silently installs it with root. Typically lands on-device
within a few seconds of the CI run finishing.

This only works because the Thor is rooted: `pm install -r` run from a root
shell skips the normal "install unknown apps" prompt entirely.

## One-time setup (on the Thor, in Termux)

Use **Termux from F-Droid** (the Play Store build is unmaintained and
network calls are often broken on it):
https://f-droid.org/packages/com.termux/

Also install **Termux:Boot** from F-Droid if you want the watcher to
survive a reboot: https://f-droid.org/packages/com.termux.boot/

### 1. Packages

```sh
pkg update
pkg install curl jq unzip git tsu termux-api
```

(`tsu` gives Termux a `su` wrapper; when you first run something with `su`
your root manager (Magisk/KernelSU/etc.) will prompt to grant Termux
superuser access — allow it. `termux-api` is optional, only used for the
"build installed" notification.)

### 2. Get the repo onto the device

```sh
git clone https://github.com/AndrewOCC/SDD-Major-Project-Game.git
cd SDD-Major-Project-Game
chmod +x tools/ayn-thor-autoinstall/install-watcher.sh
```

Cloning a private repo over HTTPS will prompt for a username/password —
use your GitHub username and the fine-grained token from step 3 as the
password (`git` will offer to remember it via its credential store).

### 3. Create a GitHub token

GitHub → Settings → Developer settings → **Fine-grained tokens** → Generate
new token:
- Repository access: **only** `AndrewOCC/SDD-Major-Project-Game`
- Permissions: **Actions → Read-only** (Metadata read-only is included
  automatically)
- No other permissions needed.

Save it on the device:

```sh
mkdir -p ~/.config/ayn-thor-autoinstall
echo "PASTE_YOUR_TOKEN_HERE" > ~/.config/ayn-thor-autoinstall/github-token
chmod 600 ~/.config/ayn-thor-autoinstall/github-token
```

### 4. Set the ntfy topic

This is a shared secret between CI and the device — anyone who learns it
could spam you with fake "build ready" pings or make your device
re-download/re-install a build it already has (they can't get your actual
GitHub token or your artifacts through it), so treat it like a password and
don't commit it anywhere.

```sh
mkdir -p ~/.config/ayn-thor-autoinstall
echo "mpg-thor-f7713266ace0ab5c11759ae1" > ~/.config/ayn-thor-autoinstall/ntfy-topic
```

Then add the same value as a **repository secret** on GitHub so CI can send
the ping: Settings → Secrets and variables → Actions → New repository
secret → name it `NTFY_TOPIC`, value `mpg-thor-f7713266ace0ab5c11759ae1`.

(Generate your own random topic instead if you'd rather not reuse this
one — anything unguessable works, e.g. `openssl rand -hex 16` on any
machine.)

### 5. Test it once, manually

Pulls and installs whatever the latest green build already is, without
waiting for a new push — good for confirming root/token/paths all work:

```sh
./tools/ayn-thor-autoinstall/install-watcher.sh --once
```

Check `~/.local/state/ayn-thor-autoinstall/watcher.log` if it doesn't
install.

### 6. Run the listener

Foreground (leave this Termux session open):

```sh
termux-wake-lock
./tools/ayn-thor-autoinstall/install-watcher.sh
```

Or start it in the background and keep it running across reboots via
Termux:Boot:

```sh
mkdir -p ~/.termux/boot
cat > ~/.termux/boot/start-ayn-thor-watcher.sh <<'EOF'
#!/data/data/com.termux/files/usr/bin/bash
termux-wake-lock
cd ~/SDD-Major-Project-Game
nohup ./tools/ayn-thor-autoinstall/install-watcher.sh \
  >> ~/.local/state/ayn-thor-autoinstall/watcher.log 2>&1 &
EOF
chmod +x ~/.termux/boot/start-ayn-thor-watcher.sh
```

Run it once by hand the first time (Termux:Boot only fires on actual
device boot):

```sh
~/.termux/boot/start-ayn-thor-watcher.sh
```

Also turn off battery optimization for Termux (Android Settings → Apps →
Termux → Battery → Unrestricted), otherwise Doze will kill the background
connection after a while and pings stop arriving until you reopen Termux.

## Day to day

Once set up, nothing else is needed — push to the tracked branch (see
`BRANCH` near the top of `install-watcher.sh`, currently
`cursor/startup-crash-fix-a6be`) and the build shows up installed on the
Thor shortly after CI goes green. Update `BRANCH` in the script (and
`git pull` it onto the device) whenever you switch which branch you're
previewing.

## Troubleshooting

- `tail -f ~/.local/state/ayn-thor-autoinstall/watcher.log` while pushing a
  commit to watch the whole flow live.
- If `pm install` fails with a signature mismatch, you have a copy of the
  app installed that wasn't built from this repo's `debug.keystore`
  (e.g. a Play-signed build) — uninstall it once and the watcher will take
  over from there since every CI build is signed the same way.
- If nothing happens on push: check the `Notify device of new build` step
  in the GitHub Actions run — it silently no-ops if `NTFY_TOPIC` isn't set
  as a repository secret.
