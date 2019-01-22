#!/usr/bin/env bash
mkdir .gradle
chmod 0600 .gradle
./gradlew release -i -s -Prelease.useAutomaticVersion=true
NEW_VERSION=$(git describe --abbrev=0)
sed "/appVersion/c\\appVersion: '${NEW_VERSION}'" Chart.yaml > tempChart.yaml && mv tempChart.yaml Chart.yaml
git add . && git commit -m "Set chart version to ${NEW_VERSION} [CI SKIP]" && git push
