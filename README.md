# folderMonitor
console program to watch and process .csv files.

## How To Use
1. clone to your PC
```
git clone https://github.com/ImTema/folderMonitor.git
```
2. compile maven file (pom.xml)(IntellijIdea)
3. run program:
* in IntellijIdea choose `Main.class` and press `ctrl+f10`
* in independed option type in command line:
```
java -jar path/to/jar/folderMonitor-with-dependencies.jar path/to/config.properties
```
**path/to/config.properties** is *optional*. By default folders are `path/to/jar/`

to terminate program put `__stopnow__.csv` file in scan directory (it can be with empty content)

## Future improvements
- [ ] XML task

    input and output formats in XML:
    XSD should be created
- [ ] UI Design

    in order to show user log data.
- [ ] JDBC implementation
