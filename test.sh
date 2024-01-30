#!/bin/bash

cd shared && mvn test
cd ../go_server && mvn test
cd ../go_client && mvn test
cd ..