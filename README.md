# agora_native

A new Flutter Agora project.

## Getting Started

Step:
0. Set baseUrl
1. saveLoginInfo: to save user info and token
2. saveConfigs: to save room configs     
3. Setting configs: setBotUsers, setWhiteBoardBackground
4. joinClassRoom: join class



//Create Whiteboard with apps
0. Clone repo: https://github.com/netless-io/Whiteboard-bridge/ (Should checkout same commit with carrot.yaml in Whiteboard repo) /Current should be c9d15239c5c4a16edf13f53d79c4dc2be69cb10c
1. curl -o ./src/injectCode.ts https://raw.githubusercontent.com/netless-io/flat-native-bridge/main/injectCode.ts
2. curl -o ./esbuild.mjs https://raw.githubusercontent.com/netless-io/flat-native-bridge/main/esbuild.mjs
3. Add import '../injectCode'; to top of file src/bridge/SDK.ts //Config, remove apps are not used
4. yarn init -y
5.  Add any apps needed
    yarn add \
    @netless/app-plyr@0.2.5\
    esbuild
5.1 Edit injectCode
    
6. node esbuild.mjs
7. 