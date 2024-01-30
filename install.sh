#!/bin/bash

cd shared && mvn clean install
cd ../go_server && mvn clean install
cd ../go_client && mvn clean install
cd ..