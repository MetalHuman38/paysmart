package net.metalbrain.paysmart.utils

class SessionLockedException : IllegalStateException("🔐 Room encryption key is unavailable: session is locked.")
