#!/bin/bash

# Thay thế import
find android/src/main/kotlin/io/agora/flat -type f -name "*.kt" -exec sed -i '' 's/import androidx.hilt.navigation.compose.hiltViewModel/import io.agora.flat.ui.util.flatViewModel/g' {} \;

# Thay thế hiltViewModel() bằng flatViewModel()
find android/src/main/kotlin/io/agora/flat -type f -name "*.kt" -exec sed -i '' 's/hiltViewModel()/flatViewModel()/g' {} \;

# Thay thế EntryPointAccessors với GlobalInstanceProvider
find android/src/main/kotlin/io/agora/flat -type f -name "*.kt" -exec sed -i '' 's/EntryPointAccessors.fromApplication/GlobalInstanceProvider.get/g' {} \;
find android/src/main/kotlin/io/agora/flat -type f -name "*.kt" -exec sed -i '' 's/EntryPointAccessors.fromActivity/GlobalInstanceProvider.get/g' {} \;
find android/src/main/kotlin/io/agora/flat -type f -name "*.kt" -exec sed -i '' 's/EntryPointAccessors.fromFragment/GlobalInstanceProvider.get/g' {} \;

echo "Migration complete" 