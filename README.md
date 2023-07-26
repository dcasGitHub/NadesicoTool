NadesicoTool
=

Introduction
-
NadesicoTool is a command-line tool made in Java for the purpose of ROM hacking.
It is used to patch code and generate font data for Kidou Senkan Nadesico: Nadesico the Mission (機動戦艦ナデシコ).

It was created for research on the games data, it is fairly early in its stage of development.

Usage
-
Store the files you will be working with on the root of the project folder.
Each option is to be performed in order. Type your choice, and press enter to continue.

It can generate font images in BMP format and create table text files using OCR for ease of table creation.
It can also patch certain code in the 1ST_READ.BIN, but it does not affect much of the game. (Work-in-Progress)

It currently works with the following extracted GDI files:

Executable data:
- 1ST_READ.BIN

Font data:
- NADE16.FON
- NADE24.FON
- NADESICO.FON

You will type the name of the file that is most appropriate to use in context.

Requirements
-
- Dump of Nadesico the Mission in GDI format (with the files extracted).
- Java Runtime Environment (JRE)
- Maven

Building
-
To build the project into a JAR file, follow these steps:

1. Open your terminal or command prompt.
2. Navigate to the root directory of the project.
3. Run the following Maven command:
mvn clean install
4. Run the application using the following command:
   java -jar NadesicoTool.jar

Credits
-
Tess4J Tesseract For Java (used for OCR Japanese character recognition)

