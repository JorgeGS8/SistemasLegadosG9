      ******************************************************************
      * Author:
      * Date:
      * Purpose:
      * Tectonics: cobc
      ******************************************************************
                     IDENTIFICATION DIVISION.
       PROGRAM-ID. BANK8.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SPECIAL-NAMES.
           CRT STATUS IS KEYBOARD-STATUS.

       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TARJETAS ASSIGN TO DISK
           ORGANIZATION IS INDEXED
           ACCESS MODE IS DYNAMIC
           RECORD KEY IS TNUM
           FILE STATUS IS FST.

           SELECT INTENTOS ASSIGN TO DISK
           ORGANIZATION IS INDEXED
           ACCESS MODE IS DYNAMIC
           RECORD KEY IS INUM
           FILE STATUS IS FSI.

       DATA DIVISION.
       FILE SECTION.
       FD TARJETAS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "tarjetas.ubd".
       01 TAJETAREG.
           02 TNUM      PIC 9(16).
           02 TPIN      PIC  9(4).

       FD INTENTOS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "intentos.ubd".
       01 INTENTOSREG.
           02 INUM      PIC 9(16).
           02 IINTENTOS PIC 9(1).

       WORKING-STORAGE SECTION.
       77 FST                      PIC  X(2).
       77 FSI                      PIC  X(2).

       78 BLACK   VALUE 0.
       78 BLUE    VALUE 1.
       78 GREEN   VALUE 2.
       78 CYAN    VALUE 3.
       78 RED     VALUE 4.
       78 MAGENTA VALUE 5.
       78 YELLOW  VALUE 6.
       78 WHITE   VALUE 7.

       01 CAMPOS-FECHA.
           05 FECHA.
               10 ANO              PIC  9(4).
               10 MES              PIC  9(2).
               10 DIA              PIC  9(2).
           05 HORA.
               10 HORAS            PIC  9(2).
               10 MINUTOS          PIC  9(2).
               10 SEGUNDOS         PIC  9(2).
               10 MILISEGUNDOS     PIC  9(2).
           05 DIF-GMT              PIC S9(4).

       01 KEYBOARD-STATUS           PIC 9(4).
           88 ENTER-PRESSED          VALUE 0.
           88 ESC-PRESSED         VALUE 2005.

       77 PRESSED-KEY              PIC  9(4).
       77 CHOICE                   PIC  9(1). 
       77 PIN-ACTUAL               PIC  9(4).
       77 NUEVO-PIN                PIC  9(4).
       77 REPITE-PIN               PIC  9(4).

       LINKAGE SECTION.
       77 TNUM-LNK                 PIC  9(16). 

       SCREEN SECTION.
       01 BLANK-SCREEN.
           05 FILLER LINE 1 BLANK SCREEN BACKGROUND-COLOR BLACK.

       01 DATA-ACCEPT.
           05 PIN-ACTUAL-ACCEPT BLANK ZERO AUTO SECURE LINE 10 COL 50
               PIC 9(4) USING PIN-ACTUAL.
           05 NUEVO-PIN-ACCEPT BLANK ZERO AUTO SECURE LINE 12 COL 50
               PIC 9(4) USING NUEVO-PIN.
           05 REPITE-PIN-ACCEPT BLANK ZERO AUTO SECURE LINE 14 COL 50
               PIC 9(4) USING REPITE-PIN.

       PROCEDURE DIVISION USING TNUM-LNK.
       IMPRIMIR-CABECERA.
           SET ENVIRONMENT 'COB_SCREEN_EXCEPTIONS' TO 'Y'
           SET ENVIRONMENT 'COB_SCREEN_ESC'        TO 'Y'

           DISPLAY BLANK-SCREEN.

           DISPLAY (2,26) "Cajero Automatico UnizarBank"
               WITH FOREGROUND-COLOR IS BLUE.

           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.

           DISPLAY (4,32) DIA.
           DISPLAY (4,34) "-".
           DISPLAY (4,35) MES.
           DISPLAY (4,37) "-".
           DISPLAY (4,38) ANO.
           DISPLAY (4,44) HORAS.
           DISPLAY (4,46) ":".
           DISPLAY (4,47) MINUTOS.

       P-CAMBIO-CLAVE.
           OPEN I-O TARJETAS.
           IF FST NOT = 00
               GO TO PSYS-ERR.

           MOVE TNUM-LNK TO TNUM.
           READ TARJETAS INVALID KEY GO TO PSYS-ERR.

           OPEN I-O INTENTOS.
           IF FSI NOT = 00
               GO TO PSYS-ERR.
           MOVE TNUM-LNK TO INUM. 

           READ INTENTOS INVALID KEY GO TO PSYS-ERR.

           IF IINTENTOS = 0
             GO TO PINT-ERR.

           INITIALIZE PIN-ACTUAL.
           INITIALIZE NUEVO-PIN.
           INITIALIZE REPITE-PIN.

           DISPLAY (8,25) "Cambio de clave personal".
           DISPLAY (10,20) "Introduzca clave actual:".
           DISPLAY (12,20) "Introduzca nueva clave:".
           DISPLAY (14,20) "Repita la nueva clave:".
           DISPLAY (24,1) "Enter - Confirmar".
           DISPLAY (24,65) "ESC - Cancelar".

           ACCEPT DATA-ACCEPT ON EXCEPTION
               IF ESC-PRESSED
                   CLOSE TARJETAS
                   CLOSE INTENTOS
                   EXIT PROGRAM
               ELSE
                   GO TO P-CAMBIO-CLAVE.

           IF PIN-ACTUAL NOT = TPIN
               GO TO PPIN-ERR.

           IF NUEVO-PIN NOT = REPITE-PIN
               DISPLAY (16,20) "Las nuevas claves no coinciden"
                   WITH FOREGROUND-COLOR IS BLACK
                        BACKGROUND-COLOR IS RED
               GO TO P-CAMBIO-CLAVE.

           MOVE NUEVO-PIN TO TPIN.
           REWRITE TAJETAREG INVALID KEY GO TO PSYS-ERR.

           MOVE 3 TO IINTENTOS.
           REWRITE INTENTOSREG INVALID KEY GO TO PSYS-ERR.

           CLOSE TARJETAS.
           CLOSE INTENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY (9,20) "La clave se ha cambiado correctamente"
               WITH FOREGROUND-COLOR IS WHITE
                    BACKGROUND-COLOR IS BLACK.
           DISPLAY (24,33) "Enter - Aceptar".

           GO TO EXIT-ENTER.

       PPIN-ERR.
           SUBTRACT 1 FROM IINTENTOS.
           REWRITE INTENTOSREG INVALID KEY GO TO PSYS-ERR.

           IF IINTENTOS = 0
               CLOSE TARJETAS
               CLOSE INTENTOS
               GO TO PINT-ERR.

           CLOSE TARJETAS.
           CLOSE INTENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY (9,26) "El codigo PIN es incorrecto"
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY (11,30) "Le quedan "
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY (11,40) IINTENTOS
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY (11,42) " intentos"
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.

           DISPLAY (24,1) "Enter - Aceptar".
           DISPLAY (24,65) "ESC - Cancelar".

       PPIN-ERR-ENTER.
           ACCEPT CHOICE AT 2480
           IF ENTER-PRESSED
               GO TO P-CAMBIO-CLAVE
           ELSE
               IF ESC-PRESSED
                   GO TO IMPRIMIR-CABECERA
               ELSE
                   GO TO PPIN-ERR-ENTER.

       PINT-ERR.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY (9,20) "Se ha sobrepasado el numero de intentos"
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY (11,18) "Por su seguridad se ha bloqueado la tarjeta"
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY (12,30) "Acuda a una sucursal"
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY (24,33) "Enter - Aceptar".

       PINT-ERR-ENTER.
           ACCEPT CHOICE AT 2480
           IF ENTER-PRESSED
               EXIT PROGRAM
           ELSE
               GO TO PINT-ERR-ENTER.

       PSYS-ERR.
           CLOSE TARJETAS.
           CLOSE INTENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY (9,25) "Ha ocurrido un error interno"
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY (11,32) "Vuelva mas tarde"
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY (24,33) "Enter - Aceptar".
           GO TO PINT-ERR-ENTER.

       EXIT-ENTER.
           ACCEPT(24,80) PRESSED-KEY
           IF ENTER-PRESSED
               EXIT PROGRAM
           ELSE
               GO TO EXIT-ENTER.

