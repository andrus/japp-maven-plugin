; Java Launcher
; per http://nsis.sourceforge.net/A_slightly_better_Java_Launcher
;--------------

Name "@LONG_NAME@"
Caption "@LONG_NAME@"
@ICON@
OutFile "@OUT_FILE@"
 
SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow
RequestExecutionLevel user

!define CLASSPATH "@NAME@.jar"
!define CLASS "@MAIN_CLASS@"

!define JVM0_VERSION @JVM0@
!define JVM0_OPTIONS "@JVM0_OPTIONS@"


Section ""
  Call GetJRE
  Pop $R0

  Push $R0
  Call GetJDKVersion
  Pop $R1

  !if ${JVM0_VERSION} > 0
  IntCmp $R1 ${JVM0_VERSION} jvm0Eq jvm0Lt jvm0Gt
  jvm0Eq:
  jvm0Gt:
      StrCpy $0 '"$R0" ${JVM0_OPTIONS} -classpath "${CLASSPATH}" ${CLASS}'
      Goto run
  jvm0Lt:
      MessageBox MB_OK "No suitable Java version found on your system!$\r$\nThis program requires Java ${JVM0_VERSION} or later."
      Goto fail
  !else
  StrCpy $0 '"$R0" ${JVM0_OPTIONS} -classpath "${CLASSPATH}" ${CLASS}'
  !endif

  run:
  SetOutPath $EXEDIR
  Exec $0

  fail:
SectionEnd
 
Function GetJRE
;
;  Find JRE (javaw.exe)
;  1 - in .\jre directory (JRE Installed with application)
;  2 - in JAVA_HOME environment variable
;  3 - in the registry
;  4 - assume javaw.exe in current dir or PATH
 
  Push $R0
  Push $R1
 
  ClearErrors
  StrCpy $R0 "$EXEDIR\jre\bin\javaw.exe"
  IfFileExists $R0 JreFound
  StrCpy $R0 ""
 
  ClearErrors
  ReadEnvStr $R0 "JAVA_HOME"
  StrCpy $R0 "$R0\bin\javaw.exe"
  IfErrors 0 JreFound
 
  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  StrCpy $R0 "$R0\bin\javaw.exe"
  IfErrors 0 JreFound

  SearchPath $R0 "javaw.exe"

 JreFound:
  Pop $R1
  Exch $R0
FunctionEnd

; Get major Java version for the given java executable
Function GetJDKVersion
  Pop $R0

  ClearErrors
  GetDllVersion $R0 $R1 $R2
  ; If version info is unreadable, assume any version is acceptable
  IfErrors versionUnknown 0

  IntOp $R3 $R1 / 0x00010000
  IntOp $R4 $R1 & 0x0000FFFF

  IntCmp $R3 1 eq1 lt1 gt1
    eq1:
        Push $R4
        Goto done
    gt1:
        Push $R3
        Goto done
    lt1:
  versionUnknown:
        Push 999
        Goto done
  done:

FunctionEnd