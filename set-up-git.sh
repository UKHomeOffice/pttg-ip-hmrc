#!/usr/bin/env bash
mkdir ~/.ssh/ && echo "$GITHUB_SSH_KEY" > ~/.ssh/id_rsa && chmod 0600 ~/.ssh/id_rsa
ssh-keyscan github.com >> ~/.ssh/known_hosts && chmod 600 ~/.ssh/known_hosts
printf  "Host github.com\n   Hostname github.com\n   IdentityFile /home/app/.ssh/id_rsa\n" > ~/.ssh/config
chmod 0600 ~/.ssh/config
printf "[github]\n[user]\n user = git" > ~/.gitconfig
git checkout EE-12943-update-helm-chart
