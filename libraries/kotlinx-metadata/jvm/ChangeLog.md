# kotlinx-metadata-jvm

## 0.0.4

- [`KT-25223`](https://youtrack.jetbrains.com/issue/KT-25223) Add JvmFunctionExtensionVisitor.visitEnd

## 0.0.3

- Support metadata of local delegated properties (see `JvmDeclarationContainerExtensionVisitor.visitLocalDelegatedProperty`)
- [`KT-24881`](https://youtrack.jetbrains.com/issue/KT-24881) Use correct class loader in kotlinx-metadata to load MetadataExtensions implementations
- [`KT-24945`](https://youtrack.jetbrains.com/issue/KT-24945) Relocate package org.jetbrains.kotlin to fix IllegalAccessError in annotation processing

## 0.0.2

- Change group ID from `org.jetbrains.kotlin` to `org.jetbrains.kotlinx`
- Depend on a specific version of kotlin-stdlib from Maven Central instead of snapshot from Sonatype Nexus
- Use `JvmMethodSignature` and `JvmFieldSignature` to represent JVM signatures instead of plain strings

## 0.0.1

- Initial release
