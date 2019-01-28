#!/bin/sh
set -e
NEW_VERSION=$(sed -e 's/^version=//' -e 's/-SNAPSHOT//' gradle.properties)
sed "/appVersion/c\\appVersion: '$NEW_VERSION'" Chart.yaml > tempChart.yaml && mv tempChart.yaml Chart.yaml
git add Chart.yaml
