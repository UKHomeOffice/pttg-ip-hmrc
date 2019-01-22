#!/usr/bin/env bash
mkdir /home/app/.ssh/ && echo "$GITHUB_SSH_KEY" > /home/app/.ssh/id_rsa && chmod 0600 /home/app/.ssh/id_rsa
ssh-keyscan github.com >> /home/app/.ssh/known_hosts && chmod 600 /home/app/.ssh/known_hosts
printf  "Host github.com\n   Hostname github.com\n   IdentityFile /home/app/.ssh/id_rsa\n" > /home/app/.ssh/config
chmod 0600 /home/app/.ssh/config
printf "[github]\n[user]\n user = git" > /home/app/.gitconfig

