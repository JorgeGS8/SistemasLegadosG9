       IDENTIFICATION DIVISION.
       PROGRAM-ID. BANK6.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SPECIAL-NAMES.
           CRT STATUS IS KEYBOARD-STATUS.

       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TARJETAS ASSIGN TO DISK
           ORGANIZATION IS INDEXED
           ACCESS MODE IS DYNAMIC
           RECORD KEY IS TNUM-E
           FILE STATUS IS FST.

           SELECT F-MOVIMIENTOS ASSIGN TO DISK
           ORGANIZATION IS INDEXED
           ACCESS MODE IS DYNAMIC
           RECORD KEY IS MOV-NUM
           FILE STATUS IS FSM.

           SELECT F-TRANSFERENCIAS ASSIGN TO DISK
           ORGANIZATION IS INDEXED
           ACCESS MODE IS DYNAMIC
           RECORD KEY IS TRF-NUM
           FILE STATUS IS FSTF.


       DATA DIVISION.
       FILE SECTION.
       
       FD TARJETAS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "tarjetas.ubd".
       01 TAJETAREG.
           02 TNUM-E      PIC 9(16).
           02 TPIN-E      PIC  9(4).
       FD F-MOVIMIENTOS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "movimientos.ubd".
       01 MOVIMIENTO-REG.
           02 MOV-NUM              PIC  9(35).
           02 MOV-TARJETA          PIC  9(16).
           02 MOV-ANO              PIC   9(4).
           02 MOV-MES              PIC   9(2).
           02 MOV-DIA              PIC   9(2).
           02 MOV-HOR              PIC   9(2).
           02 MOV-MIN              PIC   9(2).
           02 MOV-SEG              PIC   9(2).
           02 MOV-IMPORTE-ENT      PIC  S9(7).
           02 MOV-IMPORTE-DEC      PIC   9(2).
           02 MOV-CONCEPTO         PIC  X(35).
           02 MOV-SALDOPOS-ENT     PIC  S9(9).
           02 MOV-SALDOPOS-DEC     PIC   9(2).
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
       77 FST                      PIC   X(2).
       77 FSM                      PIC   X(2).
       77 FSTF                     PIC   X(2).

       78 BLACK                  VALUE      0.
       78 BLUE                   VALUE      1.
       78 GREEN                  VALUE      2.
       78 CYAN                   VALUE      3.
       78 RED                    VALUE      4.
       78 MAGENTA                VALUE      5.
       78 YELLOW                 VALUE      6.
       78 WHITE                  VALUE      7.

       01 CAMPOS-FECHA.
           05 FECHA.
               10 ANO              PIC   9(4).
               10 MES              PIC   9(2).
               10 DIA              PIC   9(2).
           05 HORA.
               10 HORAS            PIC   9(2).
               10 MINUTOS          PIC   9(2).
               10 SEGUNDOS         PIC   9(2).
               10 MILISEGUNDOS     PIC   9(2).
           05 DIF-GMT              PIC  S9(4).

       01 KEYBOARD-STATUS          PIC  9(4).
           88 ENTER-PRESSED      VALUE     0.
           88 PGUP-PRESSED       VALUE  2001.
           88 PGDN-PRESSED       VALUE  2002.
           88 UP-ARROW-PRESSED   VALUE  2003.
           88 DOWN-ARROW-PRESSED VALUE  2004.
           88 ESC-PRESSED        VALUE  2005.

       77 PRESSED-KEY              PIC   9(4).

       77 LAST-MOV-NUM             PIC  9(35).
       77 LAST-USER-ORD-MOV-NUM    PIC  9(35).
       77 LAST-USER-DST-MOV-NUM    PIC  9(35).
       77 LAST-TRF-NUM             PIC  9(35).

       77 EURENT-USUARIO           PIC  S9(7).
       77 EURDEC-USUARIO           PIC   9(2).
       77 CUENTA-DESTINO           PIC  9(16).
       77 NOMBRE-DESTINO           PIC  X(35).

       77 CENT-SALDO-ORD-USER      PIC  S9(9).
       77 CENT-SALDO-DST-USER      PIC  S9(9).
       77 CENT-IMPOR-USER          PIC  S9(9).

       77 MSJ-ORD                  PIC  X(35) VALUE "Transferimos".
       77 MSJ-DST                  PIC  X(35) VALUE "Nos transfieren".
       77 TIPO-TRF                 PIC  X(1).
       77 DIA-TRF                  PIC  9(2).
       77 MES-TRF                  PIC  9(2).
       77 ANO-TRF                  PIC  9(4).
       77 FECHA-EJEC-TRF           PIC  9(8).
       77 FECHA-HOY                PIC  9(8).

       LINKAGE SECTION.
       77 TNUM                     PIC  9(16).

       SCREEN SECTION.
       01 BLANK-SCREEN.
           05 FILLER LINE 1 BLANK SCREEN BACKGROUND-COLOR BLACK.

       01 FILTRO-CUENTA.
           05 FILLER BLANK WHEN ZERO AUTO UNDERLINE
               LINE 12 COL 54 PIC 9(16) USING CUENTA-DESTINO.
           05 FILLER AUTO UNDERLINE
               LINE 14 COL 54 PIC X(15) USING NOMBRE-DESTINO.
           05 FILLER BLANK ZERO AUTO UNDERLINE
               *>SIGN IS LEADING SEPARATE
               LINE 16 COL 54 PIC 9(7) USING EURENT-USUARIO.
           05 FILLER BLANK ZERO UNDERLINE
               LINE 16 COL 63 PIC 9(2) USING EURDEC-USUARIO.

       01 SALDO-DISPLAY.
           05 FILLER SIGN IS LEADING SEPARATE
               LINE 10 COL 33 PIC -9(7) FROM MOV-SALDOPOS-ENT.
           05 FILLER LINE 10 COL 41 VALUE ",".
           05 FILLER LINE 10 COL 42 PIC 99 FROM MOV-SALDOPOS-DEC.
           05 FILLER LINE 10 COL 45 VALUE "EUR".
        
       01 SELECCION-TIPO.
           05 FILLER LINE 18 COL 19 VALUE "Tipo de transferencia:".
           05 FILLER LINE 19 COL 19 VALUE "(P)untual / (M)ensual:".
           05 TIPO-ACCEPT LINE 19 COL 45 PIC X(1) USING TIPO-TRF.

       01 FECHA-TRF-SCREEN.
           05 FILLER LINE 20 COL 19 VALUE "Fecha (DD/MM/AAAA):".
           05 FILLER LINE 20 COL 40 PIC 9(2) USING DIA-TRF.
           05 FILLER LINE 20 COL 42 VALUE "/".
           05 FILLER LINE 20 COL 43 PIC 9(2) USING MES-TRF.
           05 FILLER LINE 20 COL 45 VALUE "/".
           05 FILLER LINE 20 COL 46 PIC 9(4) USING ANO-TRF.

       01 DIA-MES-SCREEN.
           05 FILLER LINE 20 COL 19 VALUE "Dia del mes (1-31):".
           05 FILLER LINE 20 COL 40 PIC 9(2) USING DIA-TRF.


       PROCEDURE DIVISION USING TNUM.
       INICIO.
           SET ENVIRONMENT 'COB_SCREEN_EXCEPTIONS' TO 'Y'.

           INITIALIZE CUENTA-DESTINO.
           INITIALIZE NOMBRE-DESTINO.
           INITIALIZE EURENT-USUARIO.
           INITIALIZE EURDEC-USUARIO.
           INITIALIZE LAST-MOV-NUM.
           INITIALIZE LAST-USER-ORD-MOV-NUM.
           INITIALIZE LAST-USER-DST-MOV-NUM.
           INITIALIZE TIPO-TRF.
           INITIALIZE DIA-TRF.
           INITIALIZE MES-TRF.
           INITIALIZE ANO-TRF.
           INITIALIZE LAST-TRF-NUM.

           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.
           COMPUTE FECHA-HOY = (ANO * 10000) + (MES * 100) + DIA.
           PERFORM ABRIR-TRANSFERENCIAS THRU ABRIR-TRANSFERENCIAS.

       IMPRIMIR-CABECERA.
           DISPLAY BLANK-SCREEN.
           DISPLAY(2,26) "Cajero Automatico UnizarBank"
               WITH FOREGROUND-COLOR IS 1.

           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.

           DISPLAY(4,32) DIA.
           DISPLAY(4,34) "-".
           DISPLAY(4,35) MES.
           DISPLAY(4,37) "-".
           DISPLAY(4,38) ANO.
           DISPLAY(4,44) HORAS.
           DISPLAY(4,46) ":".
           DISPLAY(4,47) MINUTOS.

        ABRIR-TRANSFERENCIAS.
           OPEN I-O F-TRANSFERENCIAS.
           IF FSTF NOT = "00" AND FSTF NOT = "30"
               OPEN OUTPUT F-TRANSFERENCIAS
               CLOSE F-TRANSFERENCIAS
               OPEN I-O F-TRANSFERENCIAS
           END-IF.
           IF FSTF NOT = "00" AND FSTF NOT = "30"
               DISPLAY "ERROR AL ABRIR transferencias.ubd: " FSTF
               GO TO PSYS-ERR
           END-IF.

           MOVE 0 TO LAST-TRF-NUM.
       LEER-TRF.
           READ F-TRANSFERENCIAS NEXT RECORD
               AT END GO FIN-LEER-TRF.
           IF TRF-NUM > LAST-TRF-NUM
               MOVE TRF-NUM TO LAST-TRF-NUM
           END-IF.
           GO TO LEER-TRF.
       FIN-LEER-TRF.
           CLOSE F-TRANSFERENCIAS.

       MOVIMIENTOS-OPEN.
           OPEN I-O F-MOVIMIENTOS.
           IF FSM NOT = "00" AND FSM NOT = "30"
               GO TO PSYS-ERR
           END-IF.

       LECTURA-MOVIMIENTOS.
           READ F-MOVIMIENTOS NEXT RECORD AT END GO TO ORDENACION-TRF.
           IF MOV-TARJETA = TNUM THEN
               IF LAST-USER-ORD-MOV-NUM < MOV-NUM THEN
                   MOVE MOV-NUM TO LAST-USER-ORD-MOV-NUM
               END-IF
           END-IF.
           IF LAST-MOV-NUM < MOV-NUM THEN
               MOVE MOV-NUM TO LAST-MOV-NUM
           END-IF.
           GO TO LECTURA-MOVIMIENTOS.

              ORDENACION-TRF.
           CLOSE F-MOVIMIENTOS.

           DISPLAY(8,30) "Ordenar Transferencia".
           DISPLAY(10,19) "Saldo Actual:".

           DISPLAY(24,2) "Enter - Confirmar".
           DISPLAY(24,66) "ESC - Cancelar".

           IF LAST-USER-ORD-MOV-NUM = 0 THEN
               MOVE 0 TO MOV-SALDOPOS-ENT
               MOVE 0 TO MOV-SALDOPOS-DEC
               COMPUTE CENT-SALDO-ORD-USER = 0
               DISPLAY(10,51) "0"
               DISPLAY(10,52) "."
               DISPLAY(10,53) "00"
               DISPLAY(10,54) "EUR"
               GO TO INDICAR-CTA-DST
           END-IF.

           MOVE LAST-USER-ORD-MOV-NUM TO MOV-NUM.
           PERFORM MOVIMIENTOS-OPEN THRU MOVIMIENTOS-OPEN.
           READ F-MOVIMIENTOS INVALID KEY GO PSYS-ERR.
           DISPLAY SALDO-DISPLAY.
           COMPUTE CENT-SALDO-ORD-USER = (MOV-SALDOPOS-ENT * 100)
                                         + MOV-SALDOPOS-DEC.
           CLOSE F-MOVIMIENTOS.

       INDICAR-CTA-DST.
           DISPLAY(12,19) "Indica la cuenta destino".
           DISPLAY(14,19) "y nombre del titular".
           DISPLAY(16,19) "Indique la cantidad a transferir".
           DISPLAY(16,61) ",".
           DISPLAY(16,66) "EUR".

           ACCEPT FILTRO-CUENTA ON EXCEPTION
           IF ESC-PRESSED THEN
               EXIT PROGRAM
           ELSE
               GO TO INDICAR-CTA-DST
           END-IF.

           COMPUTE CENT-IMPOR-USER = (EURENT-USUARIO * 100)
                                     + EURDEC-USUARIO.

           IF CENT-IMPOR-USER > CENT-SALDO-ORD-USER THEN
                   DISPLAY(20,19) "Indique una cantidad menor!!"
                    WITH BACKGROUND-COLOR RED
                   GO TO INDICAR-CTA-DST
           END-IF.

           GO TO INDICAR-TIPO.

       INDICAR-TIPO.
           DISPLAY SELECCION-TIPO.
           ACCEPT TIPO-ACCEPT ON EXCEPTION
               IF ESC-PRESSED THEN
                   EXIT PROGRAM
               END-IF
           END-ACCEPT.

           IF TIPO-TRF NOT = "P" AND TIPO-TRF NOT = "M" THEN
               DISPLAY(22,19) "Tipo incorrecto. Use P o M"
                   WITH BACKGROUND-COLOR RED
               GO TO INDICAR-TIPO
           END-IF.

           IF TIPO-TRF = "P" THEN
               GO TO PEDIR-FECHA
           ELSE
               GO TO PEDIR-DIA-MES
           END-IF.

       PEDIR-FECHA.
           DISPLAY FECHA-TRF-SCREEN.
           ACCEPT FECHA-TRF-SCREEN ON EXCEPTION
               IF ESC-PRESSED THEN
                   EXIT PROGRAM
               END-IF
           END-ACCEPT.

           COMPUTE FECHA-EJEC-TRF = (ANO-TRF * 10000)
                                    + (MES-TRF * 100)
                                    + DIA-TRF.
           GO TO REALIZAR-TRF-VERIFICACION.


       PEDIR-DIA-MES.
           DISPLAY DIA-MES-SCREEN.
           ACCEPT DIA-MES-SCREEN ON EXCEPTION
               IF ESC-PRESSED THEN
                   EXIT PROGRAM
               END-IF
           END-ACCEPT.

           IF DIA-TRF < 1 OR DIA-TRF > 31 THEN
               DISPLAY(22,19) "Dia incorrecto (1-31)"
                   WITH BACKGROUND-COLOR RED
               GO TO PEDIR-DIA-MES
           END-IF.
           MOVE 0 TO FECHA-EJEC-TRF.
           GO TO REALIZAR-TRF-VERIFICACION.

       REALIZAR-TRF-VERIFICACION.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY(08,30) "Ordenar Transferencia".
           DISPLAY(11,19) "Va a transferir:".
           DISPLAY(11,38) EURENT-USUARIO.
           DISPLAY(11,45) ".".
           DISPLAY(11,46) EURDEC-USUARIO.
           DISPLAY(11,49) "EUR de su cuenta".
           DISPLAY(12,19) "a la cuenta cuyo titular es".
           DISPLAY(12,48) NOMBRE-DESTINO.
           IF TIPO-TRF = "P" THEN
               DISPLAY(13,19) "Tipo: Puntual  Fecha:"
               DISPLAY(13,45) DIA-TRF
               DISPLAY(13,47) "/"
               DISPLAY(13,48) MES-TRF
               DISPLAY(13,50) "/"
               DISPLAY(13,51) ANO-TRF
           ELSE
               DISPLAY(13,19) "Tipo: Mensual  Dia del mes:"
               DISPLAY(13,48) DIA-TRF
           END-IF.


           DISPLAY(24,2) "Enter - Confirmar".
           DISPLAY(24,66) "ESC - Cancelar".

       ENTER-VERIFICACION.
           ACCEPT PRESSED-KEY
               ON EXCEPTION
                   IF ESC-PRESSED
                       EXIT PROGRAM
                   END-IF
           END-ACCEPT.

        VERIFICACION-CTA-CORRECTA.
           OPEN I-O TARJETAS.
           IF FST NOT = "00" AND FST NOT = "30"
              GO TO PSYS-ERR.

           MOVE CUENTA-DESTINO TO TNUM-E.
           READ TARJETAS INVALID KEY GO TO USER-BAD.
           CLOSE TARJETAS.

           PERFORM MOVIMIENTOS-OPEN THRU MOVIMIENTOS-OPEN.
           MOVE 0 TO MOV-NUM.
           MOVE 0 TO LAST-USER-DST-MOV-NUM.

       LECTURA-SALDO-DST.
           READ F-MOVIMIENTOS NEXT RECORD AT END GO TO GUARDAR-TRF.
           IF MOV-TARJETA = CUENTA-DESTINO THEN
               IF LAST-USER-DST-MOV-NUM < MOV-NUM THEN
                   MOVE MOV-NUM TO LAST-USER-DST-MOV-NUM
               END-IF
           END-IF.
           GO TO LECTURA-SALDO-DST.

         GUARDAR-TRF.
           CLOSE F-MOVIMIENTOS.
           IF LAST-USER-DST-MOV-NUM = 0
               MOVE 0 TO CENT-SALDO-DST-USER
           ELSE
               MOVE LAST-USER-DST-MOV-NUM TO MOV-NUM
               PERFORM MOVIMIENTOS-OPEN THRU MOVIMIENTOS-OPEN
               READ F-MOVIMIENTOS INVALID KEY GO PSYS-ERR
               COMPUTE CENT-SALDO-DST-USER = (MOV-SALDOPOS-ENT * 100)
                                             + MOV-SALDOPOS-DEC
               CLOSE F-MOVIMIENTOS
           END-IF.

           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.

           IF TIPO-TRF = "P" AND FECHA-EJEC-TRF = FECHA-HOY THEN
               OPEN I-O F-MOVIMIENTOS
               ADD 1 TO LAST-MOV-NUM
               MOVE LAST-MOV-NUM   TO MOV-NUM
               MOVE TNUM           TO MOV-TARJETA
               MOVE ANO            TO MOV-ANO
               MOVE MES            TO MOV-MES
               MOVE DIA            TO MOV-DIA
               MOVE HORAS          TO MOV-HOR
               MOVE MINUTOS        TO MOV-MIN
               MOVE SEGUNDOS       TO MOV-SEG
               MULTIPLY -1 BY EURENT-USUARIO
               MOVE EURENT-USUARIO TO MOV-IMPORTE-ENT
               MULTIPLY -1 BY EURENT-USUARIO
               MOVE EURDEC-USUARIO TO MOV-IMPORTE-DEC
               MOVE MSJ-ORD        TO MOV-CONCEPTO
               SUBTRACT CENT-IMPOR-USER FROM CENT-SALDO-ORD-USER
               COMPUTE MOV-SALDOPOS-ENT = (CENT-SALDO-ORD-USER / 100)
               MOVE FUNCTION MOD(CENT-SALDO-ORD-USER, 100)
                   TO MOV-SALDOPOS-DEC
               WRITE MOVIMIENTO-REG INVALID KEY GO TO PSYS-ERR
               ADD 1 TO LAST-MOV-NUM
               MOVE LAST-MOV-NUM   TO MOV-NUM
               MOVE CUENTA-DESTINO TO MOV-TARJETA
               MOVE ANO            TO MOV-ANO
               MOVE MES            TO MOV-MES
               MOVE DIA            TO MOV-DIA
               MOVE HORAS          TO MOV-HOR
               MOVE MINUTOS        TO MOV-MIN
               MOVE SEGUNDOS       TO MOV-SEG
               MOVE EURENT-USUARIO TO MOV-IMPORTE-ENT
               MOVE EURDEC-USUARIO TO MOV-IMPORTE-DEC
               MOVE MSJ-DST        TO MOV-CONCEPTO
               ADD CENT-IMPOR-USER TO CENT-SALDO-DST-USER
               COMPUTE MOV-SALDOPOS-ENT = (CENT-SALDO-DST-USER / 100)
               MOVE FUNCTION MOD(CENT-SALDO-DST-USER, 100)
                   TO MOV-SALDOPOS-DEC
               WRITE MOVIMIENTO-REG INVALID KEY GO TO PSYS-ERR
               CLOSE F-MOVIMIENTOS
           END-IF.

           OPEN I-O F-TRANSFERENCIAS.
           IF FSTF NOT = "00" AND FSTF NOT = "30"
               OPEN OUTPUT F-TRANSFERENCIAS
               CLOSE F-TRANSFERENCIAS
               OPEN I-O F-TRANSFERENCIAS
           END-IF.

           ADD 1 TO LAST-TRF-NUM.
           MOVE LAST-TRF-NUM   TO TRF-NUM.
           MOVE TNUM           TO TRF-TARJETA-ORIG.
           MOVE CUENTA-DESTINO TO TRF-CUENTA-DST.
           MOVE NOMBRE-DESTINO TO TRF-NOMBRE-DST.
           MOVE EURENT-USUARIO TO TRF-IMPORTE-ENT.
           MOVE EURDEC-USUARIO TO TRF-IMPORTE-DEC.
           MOVE TIPO-TRF       TO TRF-TIPO.
           IF TIPO-TRF = "P"
               MOVE FECHA-EJEC-TRF TO TRF-FECHA-EJEC
               MOVE 0              TO TRF-DIA-MES
               MOVE "E"            TO TRF-ESTADO
           ELSE
               MOVE 0              TO TRF-FECHA-EJEC
               MOVE DIA-TRF        TO TRF-DIA-MES
               MOVE "P"            TO TRF-ESTADO
           END-IF.
           MOVE FECHA-HOY      TO TRF-FECHA-CREAC.
           WRITE TRANSFERENCIA-REG INVALID KEY GO TO PSYS-ERR.
           CLOSE F-TRANSFERENCIAS.

       P-EXITO.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY(8,30) "Ordenar transferencia".
           DISPLAY(11,19) "Transferencia realizada correctamente!".
           DISPLAY(24,33) "Enter - Aceptar".

       P-EXITO-ENTER.
           ACCEPT PRESSED-KEY AT 2480
           IF ENTER-PRESSED
               EXIT PROGRAM
           ELSE
               GO TO P-EXITO-ENTER.

       PSYS-ERR.
           CLOSE TARJETAS.
           CLOSE F-MOVIMIENTOS.
           CLOSE F-TRANSFERENCIAS.


           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY(09,25) "Ha ocurrido un error interno"
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY(11,32) "Vuelva mas tarde"
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY(24,33) "Enter - Aceptar".

       EXIT-ENTER.
           ACCEPT(24,80) PRESSED-KEY
           IF ENTER-PRESSED
               EXIT PROGRAM
           ELSE
               GO TO EXIT-ENTER.

       USER-BAD.
           CLOSE TARJETAS.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY(9,22) "La cuenta introducida es incorrecta"
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY(24,33) "Enter - Salir".
           GO TO EXIT-ENTER.
