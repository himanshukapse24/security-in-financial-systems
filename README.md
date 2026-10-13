This project is a Java multithreaded framework developed to have a defense for banking data streams from inside threats that includes tamper with transaction logs on file and streaming data. 
By using BouncyCastle HMAC-SHA256 cryptographic signatures, the system continuously checks the files at the binary byte layer, detects any unauthorized changes, and quickly performs a self-healing rollback within 3.5 seconds to restore the data back to its original clean state. 
This complete framework checks that our downstream RandomForest machine learning model always trains on pure, unpolluted data, preventing the system from processing on fraud-detection unseen areas.
It also detects any synthetic data injection attacks, which are used as insider manipulations for machine learning models. 


To run this framework, clone the repository, place the CSV file in root folder, then run following commands in terminal:
1. brew install maven
2. mvn exec:java -Dexec.mainClass="com.dcu.practicum.MainApplication" -Dexec.args="--profile protected --dataset dataset3.csv"