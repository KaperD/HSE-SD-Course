# hsesh — simple shell
[![codecov](https://codecov.io/gh/KaperD/HSE-SD-Course/branch/implementation-task/graph/badge.svg?token=XK9YMS2NLZ)](https://codecov.io/gh/KaperD/HSE-SD-Course)

## Build and run
We use `Gradle` as build system

For Linux and MacOS:
```shell
./gradlew build # build project
./gradlew installDist # make executable script
./build/install/hsesh/bin/hsesh # run our shell
```

For Windows:
```shell
.\gradlew.bat build # build project
.\gradlew.bat installDist # make executable script
.\build\install\hsesh\bin\hsesh.bat # run our shell
```

## Features

### Commands
hsesh provides 5 commands:
- echo [string ...] — write arguments to the standard output
- cat [file ...] — concatenate and print files
- wc [file ...] — word, line, character, and byte count of given files
- pwd — write working directory name to the standard output
- exit — exit hsesh

Example:
```shell
> echo ”Hello, world!”
Hello, world!
> FILE=example.txt
> cat $FILE
Some example text
> cat example.txt | wc
1 3 18
> echo 123 | wc
1 1 4
> pwd
/Users/user1/
> x=ex
> y=it
> $x$y
Bye
```

Additionally, you can use other programs installed in your machine

Example:
```shell
> git status
fatal: not a git repository (or any of the parent directories): .git
> git init
Initialized empty Git repository in /Users/user1/.git/
```

### Quotes
hsesh supports single and double quotes. Difference between them is that in single quotes variables will not be replaced with their values. Also you can use concatenation of different quotes

Example:
```shell
> a=4
> echo 3    $a
3 4
> echo "3    $a"
3    4
> echo '3    $a'
3    $a
echo '$a'="$a"
$a=4
```

### Variables
hsesh supports declaring new variables. Additionally, it inherits variables from parent process

Example:
```shell
> echo $HOME
/Users/user1
> a=3
> b=4
> c=$a' != '$b
> echo $c
3 != 4
> HOME=5
> echo $HOME
5
```

### Pipes

hsesh supports pipes of commands. Commands in pipe are executed sequentially, standard output from one command goes to next command standard input. If any command fails (exit code != 0), pipe stops

Example:
```shell
> echo 123 | wc
1 1 4
> echo 123 | pwd | echo 3
3
> echo 123 | pwd 3 | exit
pwd: too many arguments
> echo 123 | pwd | exit
Bye
```
