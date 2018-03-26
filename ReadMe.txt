NetSDK Android Development Kit Description

1). Dependents
  - The P2P Login Module depends on the P2P library.
  - The functionality involved in playback and monitoring depend on the PlaySDK library.    
  - WifiConfig，Device connect Wifi，config device to Wifi depend on the SmartConfig library.    

  - Path of jar and library： NetSDK_Chn_Android\DemoSource\AndroidDemo\app\libs
  - NetSDK jar：           INetSDK.jar      
  - NetSDK library：	   libconfigsdk.so  libjninetsdk.so  libnetsdk.so

  - PlaySDK jar：          IPlaySDK.jar 
  - PlaySDK library：      libplay.so   libjniplay.so   libgnustl_shared.so   libhwdec.so

  - P2P jar ：             libToUProxy.jar  
  - P2P library：          libToUProxy.so
			 
  - SmartConfig jar：      SmartConfig.jar
  - SmartConfig library：  libjnismartconfig.so
    
2). The Directories
  - Demo Directory
    Includes installable apk file and Demo snapshots.
    
  - DemoSource Directory
    Includes AndroidDemo sample source code.
    
  - ProgrammingManual Directory
    Includes the documentation of INetSDK.jar
    
3). Precautions
  - Requires other configuration of the module's build.gradle to load *.so.
    In build.gradle, modify as follows:
    android {
        ...
        sourceSets {
            main {
                jniLibs.srcDirs = ['libs']
            }
        }
    }  
    