#!/bin/bash
set -e
pip install -r requirements.txt -t ./package
cp lambda_function.py ./package/
cd package && zip -r ../lambda.zip . && cd ..
echo "Built lambda.zip"
