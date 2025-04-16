@echo off

javac -d build/classes fr/breadeater/fileserver/FileServer.java
jar cfm build/FileServer.jar manifest.mf -C build/classes .