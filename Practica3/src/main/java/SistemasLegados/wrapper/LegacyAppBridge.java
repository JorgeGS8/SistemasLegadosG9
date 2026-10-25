// Fachada que coordina todo el ciclo: recibe una orden de negocio, adquiere el cerrojo de sincronización
// (para que dos usuarios en la web no envíen teclas a la vez), pulsa las teclas necesarias,
// captura la pantalla, parsea el texto y devuelve la información lista a la capa de servicio.