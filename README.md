![Status: Active](https://documentation.coremedia.com/badges/badge_status_active.png "Status: Active")
![For CoreMedia CMS](https://documentation.coremedia.com/badges/badge_coremedia_cms.png "For CoreMedia CMS")

![CoreMedia Labs Logo](https://documentation.coremedia.com/badges/banner_coremedia_labs_wide.png "CoreMedia Labs Logo Title Text")


# CoreMedia Studio Hub

The CoreMedia Studio Hub allows to integrate various external and asset 
management systems into the Studio library and to preview items of these 
systems. It allows you to integrate just about any external system or platform 
into your CoreMedia system. The Studio Hub is implemented as a Blueprint 
extension.

### Documentation & Tutorial

https://github.com/CoreMedia/coremedia-studio-hub/tree/master/documentation

### Issue Tracker

https://github.com/CoreMedia/coremedia-studio-hub/issues

### Installation
 
- From the project's root folder, clone this repository as submodule into the extensions folder. Make sure to use the branch name that matches your workspace version. 
```
git submodule add  -b 1907.1 https://github.com/CoreMedia/coremedia-studio-hub modules/extensions/coremedia-studio-hub
```

- Use the extension tool in the root folder of the project to link the modules into your workspace.
 ```
mvn -f workspace-configuration/extensions com.coremedia.maven:extensions-maven-plugin:LATEST:sync -Denable=coremedia-studio-hub
```

- Rebuild the workspace

For CI runs:
- Ensure that the matching branch name is set in the _.gitmodules_ file, e.g.:

```
[submodule "modules/extensions/coremedia-studio-hub"]
	path = modules/extensions/coremedia-studio-hub
	url = https://github.com/CoreMedia/coremedia-studio-hub.git
	branch = 1907.2
```

For the IDEA import:
- Ignore folder _.remote-package_
- Disable "Settings > Compiler > Clear output directory on rebuild"
