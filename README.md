# Use Agora in Native View

## Install Guide

### Android

*No specific steps provided yet.*

### iOS

#### 1. Add this script to your `Podfile`:

```ruby
pre_install do |installer|
  # Define the path for the AgoraRtm framework
  rtm_pod_path = File.join(installer.sandbox.root, 'AgoraRtm_iOS')
  
  # Full path of aosl.xcframework
  aosl_xcframework_path = File.join(rtm_pod_path, 'aosl.xcframework')
  
  # Check if the file exists, and if so, delete it
  if File.exist?(aosl_xcframework_path)
    puts "Deleting aosl.xcframework from #{aosl_xcframework_path}"
    FileUtils.rm_rf(aosl_xcframework_path)
  else
    puts "aosl.xcframework not found, skipping deletion."
  end
end
```

#### 2. If using a custom Whiteboard URL (e.g. `https://vuihoc-edtech.github.io/white_board_with_apps/`), edit the `post_install` section:

```ruby
post_install do |installer|
  installer.pods_project.targets.each do |target|
    flutter_additional_ios_build_settings(target)

    if target.name.include?('Whiteboard')
      # Remove files from build phase that are in 'Resource'
      target.resources_build_phase.files.each do |file|
        if file.file_ref.path.end_with?("Whiteboard.bundle")
          puts "Removing resource: #{file.file_ref.path}"
          file.remove_from_project
        end
      end
    end
  end
end
```
