![Status: Active](https://documentation.coremedia.com/badges/badge_status_active.png "Status: Active")
![Tested: 3.1801.2](https://documentation.coremedia.com/badges/badge_tested_coremedia_9-1801-2.png "Tested: 3.1801.2")

![CoreMedia Labs Logo](https://documentation.coremedia.com/badges/banner_coremedia_labs_wide.png "CoreMedia Labs Logo Title Text")

# Coremedia Studio Hub

The CoreMedia Studio Hub enables connections to your file, asset and content sources directly in Studio. OOTB adapters are provided for Dropbox, YouTube, RSS Feeds, S3 and file-systems. Browse, search and preview items from these systems in the Studio Library. Create beautiful user experiences with them without leaving the comfort of Studio.

Employ the convenience and power of the Studio interface to gather content from various sources. Create content via drag-and-drop from these connected sources and perfect the look of your new content with our immediate preview update. 

Apart from the ready-to-go implementations mentioned before, this extension provides a generic interface which can be used to implement adapters to just about any source, including asset management systems, like Celum; image delivery systems, like the DPA or Cloudinary; or other content management systems, such as IBM Watson Content Hub, WordPress or even other CoreMedia systems! 

The configuration is done through settings which can be conveniently created in CoreMedia Studio. To use our OOTB adapters no code needs to be changed, as all setup for connections and options is kept cleanly separated from the extension code.

Here is an overview of the steps to take to install and configure the extension. The details for these steps together with example settings can be found in the [wiki](https://github.com/CoreMedia/coremedia-studio-hub/wiki).

**1. Get the extension**

To install the extension, download the project and unpack it into the extensions folder of your workspace. The add the connector extension to your extensions.properties file. Then run the extension tool on your workspace to enable the extension and build with maven.

**2. Configure the connections**

Use the sample settings files found in the wiki and add in the values for your accounts on these systems. 

**3. Reload Studio**

Your connections should appear in the Studio Library!


*******


# CoreMedia Labs

Welcome to [CoreMedia Labs](https://blog.coremedia.com/labs/)! This repository is part of a platform for developers who want to have a look under the hood or get some hands-on understanding of the vast and compelling capabilities of CoreMedia. Whatever your experience level with CoreMedia is, we've got something for you.

Each project in our Labs platform is an extra feature to be used with CoreMedia, including extensions, tools and 3rd party integrations. We provide some test data and explanatory videos for non-customers and for insiders there is open-source code and instructions on integrating the feature into your CoreMedia workspace. 

The code we provide is meant to be example code, illustrating a set of features that could be used to enhance your CoreMedia experience. We'd love to hear your feedback on use-cases and further developments! If you're having problems with our code, please refer to our issues section. 
