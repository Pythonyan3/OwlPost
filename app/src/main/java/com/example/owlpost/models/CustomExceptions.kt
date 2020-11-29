package com.example.owlpost.models

import java.lang.Exception

class UriSchemeException(message: String?) : Exception(message) {}

class FileSizeException(message: String?) : Exception(message) {}
class AttachmentsSizeException(message: String?) : Exception(message) {}
class SettingsException(message: String?) : Exception(message) {}
class MailboxFolderException(message: String?) : Exception(message) {}