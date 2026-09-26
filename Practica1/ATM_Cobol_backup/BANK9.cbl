       IDENTIFICATION DIVISION.
       PROGRAM-ID. BANK9.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SPECIAL-NAMES.
           CRT STATUS IS KEYBOARD-STATUS.

       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT F-TRANSFERENCIAS ASSIGN TO DISK
           ORGANIZATION IS INDEXED
           ACCESS MODE IS DYNAMIC
           RECORD KEY IS TRF-NUM
           FILE STATUS IS FSTF.

       DATA DIVISION.
       FILE SECTION.
       FD F-TRANSFERENCIAS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "transferencias.ubd".
       01 TRANSFERENCIA-REG.
           02 TRF-NUM              PIC  9(35).
           02 TRF-TARJETA-ORIG     PIC  9(16).
           02 TRF-CUENTA-DST       PIC  9(16).
           02 TRF-NOMBRE-DST       PIC  X(35).
           02 TRF-IMPORTE-ENT      PIC  S9(7).
           02 TRF-IMPORTE-DEC      PIC   9(2).
           02 TRF-TIPO             PIC  X(1).
           02 TRF-FECHA-EJEC       PIC  9(8).
           02 TRF-DIA-MES          PIC  9(2).
           02 TRF-ESTADO           PIC  X(1).
           02 TRF-FECHA-CREAC      PIC  9(8).

       WORKING-STORAGE SECTION.
       77 FSTF                     PIC  X(2).

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

       01 KEYBOARD-STATUS          PIC  9(4).
           88 ENTER-PRESSED      VALUE 0.
           88 ESC-PRESSED        VALUE 2005.

       77 PRESSED-KEY              PIC  9(4).

       77 DIA1-USUARIO             PIC  9(2).
       77 MES1-USUARIO             PIC  9(2).
       77 ANO1-USUARIO             PIC  9(4).
       77 DIA2-USUARIO             PIC  9(2).
       77 MES2-USUARIO             PIC  9(2).
       77 ANO2-USUARIO             PIC  9(4).

       77 FECHA-MIN                PIC  9(8).
       77 FECHA-REF                PIC  9(8).
       77 FECHA-MAX                PIC  9(8).

       01 FECHA-VISIBLE.
           05 FV-ANO               PIC  9(4).
           05 FV-MES               PIC  9(2).
           05 FV-DIA               PIC  9(2).

       77 LINEA-ACTUAL             PIC  9(2).
       77 TOTAL-ENCONTRADAS        PIC  9(3).
       77 AUX                      PIC  9(9).

       LINKAGE SECTION.
       77 TNUM                     PIC  9(16).

       SCREEN SECTION.
       01 BLANK-SCREEN.
           05 FILLER LINE 1 BLANK SCREEN BACKGROUND-COLOR BLACK.

       01 FILTRO-FECHAS.
           05 DIA-MIN BLANK ZERO AUTO UNDERLINE
               LINE 11 COL 37 PIC 9(2) USING DIA1-USUARIO.
           05 FILLER LINE 11 COL 39 VALUE "/".
           05 MES-MIN BLANK ZERO AUTO UNDERLINE
               LINE 11 COL 40 PIC 9(2) USING MES1-USUARIO.
           05 FILLER LINE 11 COL 42 VALUE "/".
           05 ANO-MIN BLANK ZERO AUTO UNDERLINE
               LINE 11 COL 43 PIC 9(4) USING ANO1-USUARIO.
           05 DIA-MAX BLANK ZERO AUTO UNDERLINE
               LINE 11 COL 54 PIC 9(2) USING DIA2-USUARIO.
           05 FILLER LINE 11 COL 56 VALUE "/".
           05 MES-MAX BLANK ZERO AUTO UNDERLINE
               LINE 11 COL 57 PIC 9(2) USING MES2-USUARIO.
           05 FILLER LINE 11 COL 59 VALUE "/".
           05 ANO-MAX BLANK ZERO AUTO UNDERLINE
               LINE 11 COL 60 PIC 9(4) USING ANO2-USUARIO.

       PROCEDURE DIVISION USING TNUM.
       IMPRIMIR-CABECERA.
           SET ENVIRONMENT 'COB_SCREEN_EXCEPTIONS' TO 'Y'
           SET ENVIRONMENT 'COB_SCREEN_ESC'        TO 'Y'

           DISPLAY BLANK-SCREEN.
           DISPLAY(2,26) "Cajero Automatico UnizarBank"
               WITH FOREGROUND-COLOR IS BLUE.

           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.
           DISPLAY(4,32) DIA.
           DISPLAY(4,34) "-".
           DISPLAY(4,35) MES.
           DISPLAY(4,37) "-".
           DISPLAY(4,38) ANO.
           DISPLAY(4,44) HORAS.
           DISPLAY(4,46) ":".
           DISPLAY(4,47) MINUTOS.

       PCONSULTA-TRF.
           INITIALIZE DIA1-USUARIO.
           INITIALIZE MES1-USUARIO.
           INITIALIZE ANO1-USUARIO.
           INITIALIZE DIA2-USUARIO.
           INITIALIZE MES2-USUARIO.
           INITIALIZE ANO2-USUARIO.

           DISPLAY(8,25) "Listado de transferencias".
           DISPLAY(10,10) "Indique el rango de fechas a consultar:".
           DISPLAY(11,20) "Desde   /  /     hasta   /  /  ".
           DISPLAY(13,10) "(Deje en blanco para ver todas)".

           DISPLAY(24,1) "Enter - Aceptar".
           DISPLAY(24,65) "ESC - Cancelar".

           ACCEPT FILTRO-FECHAS ON EXCEPTION
               IF ESC-PRESSED
                   EXIT PROGRAM
               ELSE
                   GO TO PCONSULTA-TRF.

           COMPUTE FECHA-MIN = (ANO1-USUARIO * 10000)
                               + (MES1-USUARIO * 100)
                               + DIA1-USUARIO.
           COMPUTE FECHA-MAX = (ANO2-USUARIO * 10000)
                               + (MES2-USUARIO * 100)
                               + DIA2-USUARIO.

           IF FECHA-MIN = 0
               MOVE 00000000 TO FECHA-MIN.
           IF FECHA-MAX = 0
               MOVE 99991231 TO FECHA-MAX.

           IF FECHA-MIN > FECHA-MAX
               DISPLAY(15,15) "Fecha inicio mayor que fin"
                   WITH FOREGROUND-COLOR IS WHITE
                        BACKGROUND-COLOR IS RED
               GO TO PCONSULTA-TRF.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           PERFORM DIBUJAR-CABECERAS THRU DIBUJAR-CABECERAS.
           DISPLAY(24,33) "ESC - Salir".

           MOVE 8 TO LINEA-ACTUAL.
           MOVE 0 TO TOTAL-ENCONTRADAS.

           OPEN INPUT F-TRANSFERENCIAS.
           IF FSTF NOT = "00" AND FSTF NOT = "30"
               GO TO PSYS-ERR.

       LEER-TRF.
           READ F-TRANSFERENCIAS NEXT RECORD
               AT END GO FIN-LECTURA.

           IF TRF-TARJETA-ORIG NOT = TNUM
               GO TO LEER-TRF.

           IF TRF-TIPO = "P"
               MOVE TRF-FECHA-EJEC TO FECHA-REF
           ELSE
               MOVE TRF-FECHA-CREAC TO FECHA-REF.

           IF FECHA-REF < FECHA-MIN
               GO TO LEER-TRF.
           IF FECHA-REF > FECHA-MAX
               GO TO LEER-TRF.

           IF LINEA-ACTUAL > 21
               GO TO ESPERA-TECLA.

           PERFORM MOSTRAR-TRF THRU MOSTRAR-TRF.
           ADD 1 TO LINEA-ACTUAL.
           ADD 1 TO TOTAL-ENCONTRADAS.
           GO TO LEER-TRF.

       FIN-LECTURA.
           CLOSE F-TRANSFERENCIAS.
           IF TOTAL-ENCONTRADAS = 0
               DISPLAY(12,18) "No hay transferencias en ese rango"
                   WITH FOREGROUND-COLOR IS WHITE
                        BACKGROUND-COLOR IS BLUE.
           GO TO ESPERA-FINAL.

       ESPERA-TECLA.
           DISPLAY(24,10) "Enter - Continuar".
           DISPLAY(24,55) "ESC - Salir".
           ACCEPT PRESSED-KEY AT 2480
           IF ESC-PRESSED
               CLOSE F-TRANSFERENCIAS
               EXIT PROGRAM.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           PERFORM DIBUJAR-CABECERAS THRU DIBUJAR-CABECERAS.
           DISPLAY(24,33) "ESC - Salir".
           MOVE 8 TO LINEA-ACTUAL.
           MOVE 0 TO TOTAL-ENCONTRADAS.
           GO TO LEER-TRF.

       ESPERA-FINAL.
           DISPLAY(24,33) "Enter - Aceptar".
       ESPERA-FINAL-ENTER.
           ACCEPT PRESSED-KEY AT 2480
           IF ENTER-PRESSED OR ESC-PRESSED
               EXIT PROGRAM
           ELSE
               GO TO ESPERA-FINAL-ENTER.

       DIBUJAR-CABECERAS.
           DISPLAY(7,2)  "FECHA".
           DISPLAY(7,13) "TIP".
           DISPLAY(7,17) "EST".
           DISPLAY(7,21) "CUENTA DESTINO".
           DISPLAY(7,38) "TITULAR".
           DISPLAY(7,60) "IMPORTE".

       MOSTRAR-TRF.
           MOVE FECHA-REF TO FECHA-VISIBLE.

           DISPLAY (LINEA-ACTUAL, 2)  FV-DIA.
           DISPLAY (LINEA-ACTUAL, 4)  "/".
           DISPLAY (LINEA-ACTUAL, 5)  FV-MES.
           DISPLAY (LINEA-ACTUAL, 7)  "/".
           DISPLAY (LINEA-ACTUAL, 8)  FV-ANO.
           DISPLAY (LINEA-ACTUAL, 13) TRF-TIPO.
           DISPLAY (LINEA-ACTUAL, 17) TRF-ESTADO.
           DISPLAY (LINEA-ACTUAL, 21) TRF-CUENTA-DST.
           DISPLAY (LINEA-ACTUAL, 38) TRF-NOMBRE-DST.
           DISPLAY (LINEA-ACTUAL, 60) TRF-IMPORTE-ENT.
           DISPLAY (LINEA-ACTUAL, 68) ".".
           DISPLAY (LINEA-ACTUAL, 69) TRF-IMPORTE-DEC.
           DISPLAY (LINEA-ACTUAL, 72) "EUR".

       PSYS-ERR.
           CLOSE F-TRANSFERENCIAS.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY(9,25) "Ha ocurrido un error interno"
               WITH FOREGROUND-COLOR IS WHITE
                    BACKGROUND-COLOR IS RED.
           DISPLAY(11,32) "Vuelva mas tarde"
               WITH FOREGROUND-COLOR IS WHITE
                    BACKGROUND-COLOR IS RED.
           DISPLAY(24,33) "Enter - Aceptar".
       PSYS-ERR-ENTER.
           ACCEPT PRESSED-KEY AT 2480
           IF ENTER-PRESSED OR ESC-PRESSED
               EXIT PROGRAM
           ELSE
               GO TO PSYS-ERR-ENTER.

               