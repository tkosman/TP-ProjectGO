.PHONY: all project1 project2 clean

all: project1 project2

project1:
    @echo "Compiling client"
    cd go_client && mvn clean install

project2:
    @echo "Compiling server"
    cd go_server && mvn clean install

clean:
    @echo "Cleaning Projects"
    cd go_client && mvn clean
    cd go_server && mvn clean
