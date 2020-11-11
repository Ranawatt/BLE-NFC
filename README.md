# BLE-NFC

[![Build Status](https://travis-ci.org/amitshekhariitbhu/Fast-Android-Networking.svg?branch=master)]
[![Sugandh Ranawatt](https://img.shields.io/badge/sugandh's-opensource-blue.svg)](https://github.com/Ranawatt)
[![Sugandh's Community](https://img.shields.io/badge/join-community-blue.svg)]
[![API](https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=19)
[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=102)](https://opensource.org/licenses/Apache-2.0)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)]



### 1. Application will work in two modes
  ## 	BLE Server/Card Emulation Mode
            Screen will contain Edit Text, Text View, 3 buttons (1st for BLE, 2nd for NFC, 3rd for Encrypt)
  ##    BLE Client/NFC Reader Mode
            Screen will contain 2 Text View, 2 buttons (1st for BLE, 2nd for NFC)
	
###     BLE Communication	
   ##       2.1. Server Mode
                 User will enter any text in Edit Text
                 User will click Encrypt button, on click text will be encrypted using AES-256 Algorithms with
			   Key = 60CA0C1AC45776EF7C42C958F2A009107348D0F3B858F32691B3EABF3DC5B2FF
		     Encrypted text will be displayed on Text View
                 User will click on BLE Button. On click GATT server will setup and start advertising.
	
   ##       2.2. Client Mode
                 Screen will contain 2 Text View, 2 buttons (1st for BLE, 2nd for NFC)
                 User will click on BLE. On click, BLE Scan will start and connect to GATT Server and read encrypted data.
                 Client will decrypt data and display Encrypted Data on 1st Text View and Decrypted Text on 2nd Text View.
	
###     NFC Communication
   ##       3.1 Card Emulation Mode
                Same as step - i and ii in 2.1 for BLE.
                User will click on NFC button. On click Encrypted data will be ready for transmission through NFC.

   ##       3.2 Reader Mode
       		Same as step-i in 2.2 for BLE.
                User will click on NFC. Device will start as NFC Reader.
                Card Emulating device will be tapped on Reader device so data encrypted data will be received by reader.
                Same as step-iii in 2.2


####   Note – 
           For BLE, feel free to use any UUID for GATT Service and GATT Characteristic.
           For NFC, Use AID: A0000001010101 and create command as per your convenience.
           If your device supports BLE and NFC, then test as well otherwise complete the code and send.
