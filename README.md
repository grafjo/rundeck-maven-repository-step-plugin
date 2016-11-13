# Rundeck Maven Repository Plugin

This plugin can download a maven artifact from a given maven repository
and distributes the artifact to your managed nodes

## Usage

To use this module with the default configuration, just start with this:

```
./gradlew build
```

Copy  `build/libs/rundeck-maven-repository-step-plugin-*.jar` to your `libext` folder (e.g. /var/lib/rundeck/libext)

Restart rundeck

Go to `Installed and Bundled Plugins` and see ` Workflow Steps `


## Authors
* Johannes Graf ([@grafjo](https://github.com/grafjo))


## License

rundeck-maven-repository-step-plugin is released under the MIT License.
See the bundled LICENSE file for details.
