#!/bin/bash
set -e

echo "=== Updating packages ==="
sudo apt-get update

echo "=== Installing Google Chrome ==="
sudo apt-get install -y wget
wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo apt-get install -y ./google-chrome-stable_current_amd64.deb
rm google-chrome-stable_current_amd64.deb

echo "=== Applying the Chrome Sandbox & Shared Memory Bypass ==="
# Move the actual chrome binary out of the way
sudo dpkg-divert --add --rename --divert /usr/bin/google-chrome.real /usr/bin/google-chrome

# Create a wrapper script that injects the required flags automatically
echo '#!/bin/bash
exec /usr/bin/google-chrome.real --no-sandbox --disable-dev-shm-usage "$@"' | sudo tee /usr/bin/google-chrome
sudo chmod +x /usr/bin/google-chrome

echo "=== Preparing Antigravity Repository ==="
sudo apt-get install -y curl gnupg
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://us-central1-apt.pkg.dev/doc/repo-signing-key.gpg | gpg --dearmor | sudo tee /etc/apt/keyrings/antigravity-repo-key.gpg > /dev/null

echo "deb [signed-by=/etc/apt/keyrings/antigravity-repo-key.gpg] https://us-central1-apt.pkg.dev/projects/antigravity-auto-updater-dev/ antigravity-debian main" | sudo tee /etc/apt/sources.list.d/antigravity.list

echo "=== Installing Antigravity ==="
sudo apt-get update
sudo apt-get install -y antigravity

echo "=== Setup Complete! ==="

