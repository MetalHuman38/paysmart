# Native/JNI Pipeline Guidance

## Recommendation
Use JNI for **crypto and deterministic transforms**, not full invoice PDF/DOCX rendering.

Why:
- Rendering pipelines are I/O and layout heavy; JNI gives limited gains there.
- JNI increases memory/lifecycle complexity and crash surface.
- Current native stack already fits security-critical work (PBKDF2, password hash, Room AES).

## Good JNI use for invoices
- Canonical payload normalization/hashing before upload.
- Optional byte compression of large generated payloads before network transfer.
- Constant-time comparisons for sensitive values.

## Keep in Kotlin/Worker/API
- Weekly totals and business rules.
- PDF/DOCX template rendering.
- Share/download orchestration.

## Existing native bridge
- Kotlin:
  - `app/src/main/java/net/metalbrain/paysmart/data/native/NativeBridge.kt`
  - `app/src/main/java/net/metalbrain/paysmart/data/native/NativePasswordBridge.kt`
  - `app/src/main/java/net/metalbrain/paysmart/data/native/RoomNativeBridge.kt`
- C++:
  - `app/src/main/cpp/native-lib.cpp`
  - `app/src/main/cpp/room-lib.cpp`

## Doxygen docs generation
1. Install Doxygen and ensure `doxygen` is in PATH.
2. Run:

```powershell
pwsh app/src/main/cpp/Invoke-GenerateNativeDocs.ps1
```

3. Output:
- `app/docs/native-api/html/index.html`
