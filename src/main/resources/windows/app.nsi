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
!define ADDITIONAL_JVM_VERSION @ADDITIONAL_JVM_VERSION@
 
Section ""
  Call GetJRE
  Pop $R0

  StrCpy $0 '"$R0" @JVM_OPTIONS@ -classpath "${CLASSPATH}" ${CLASS}'

  ; If Additional JVM version check needed, perform it and add optional JVM options
  !if ${ADDITIONAL_JVM_VERSION} > 0

  Push $R0
  Call GetJDKVersion
  Pop $R1

  IntCmp $R1 ${ADDITIONAL_JVM_VERSION} jmvEq jvmLt jvmGt
  jmvEq:
  jvmGt:
      StrCpy $0 '"$R0" @JVM_OPTIONS@ @ADDITIONAL_JVM_OPTIONS@ -classpath "${CLASSPATH}" ${CLASS}'
      Goto run
  jvmLt:
      Goto run
  run:
  !endif

  SetOutPath $EXEDIR
  Exec $0
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
  StrCpy $R0 "javaw.exe"
        
 JreFound:
  Pop $R1
  Exch $R0
FunctionEnd

; Get major Java version for the given java executable
Function GetJDKVersion
  Pop $R0

  GetDllVersion $R0 $R1 $R2
  IntOp $R3 $R1 / 0x00010000
  IntOp $R4 $R1 & 0x0000FFFF
  IntOp $R5 $R2 / 0x00010000
  IntOp $R6 $R2 & 0x0000FFFF

  IntCmp $R3 1 eq1 lt1 gt1
    eq1:
        Push $R4
        Goto done
    gt1:
        Push $R3
        Goto done
    lt1:
        Push 0
        Goto done
  done:

FunctionEnd