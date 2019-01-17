    #!/usr/bin/env sh
NEW_VERSION=$(git describe --abbrev=0)
sed "/appVersion/c\\appVersion: '${NEW_VERSION}'" Chart2.yaml > tempChart.yaml && mv tempChart.yaml Chart2.yaml
git add . && git commit -m "Set chart version to ${NEW_VERSION} [CI SKIP]" && git push
