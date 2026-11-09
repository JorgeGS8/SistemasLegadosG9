const estadoEl        = document.getElementById('estado');
const resultadoEl     = document.getElementById('resultado');
const numRegistrosEl  = document.getElementById('num-registros');

function mostrarEstado(msg, tipo = 'info') {
    estadoEl.textContent = msg;
    estadoEl.className = 'estado ' + tipo;
    estadoEl.hidden = false;
}
function ocultarEstado() { estadoEl.hidden = true; }

function setCargando(btn, cargando) {
    btn.disabled = cargando;
    if (cargando) {
        btn.dataset.original = btn.textContent;
        btn.textContent = 'Cargando…';
    } else if (btn.dataset.original) {
        btn.textContent = btn.dataset.original;
    }
}

function escapeHtml(s) {
    if (s == null) return '';
    return String(s)
        .replaceAll('&', '&amp;').replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;').replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

async function cargarNumeroRegistros() {
    numRegistrosEl.textContent = '…';
    try {
        const res = await fetch('/api/registros');
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Error desconocido');
        numRegistrosEl.textContent = data.total;
    } catch (e) {
        numRegistrosEl.textContent = '--';
        mostrarEstado('No se pudo obtener el número de registros: ' + e.message, 'error');
    }
}

function renderPrograma(p) {
    return `
        <div class="detalle">
            <h3>${escapeHtml(p.nombre)}</h3>
            <dl>
                <dt>Número de registro</dt><dd>${p.numeroRegistro}</dd>
                <dt>Nombre</dt><dd>${escapeHtml(p.nombre)}</dd>
                <dt>Tipo</dt><dd>${escapeHtml(p.tipo)}</dd>
                <dt>Cinta</dt><dd>${escapeHtml(p.cinta)}</dd>
            </dl>
        </div>`;
}

function renderTabla(programas) {
    if (!programas.length) return '<p>No se han encontrado programas.</p>';
    const filas = programas.map(p => `
        <tr>
            <td>${p.numeroRegistro}</td>
            <td>${escapeHtml(p.nombre)}</td>
            <td>${escapeHtml(p.tipo)}</td>
            <td>${escapeHtml(p.cinta)}</td>
        </tr>`).join('');
    return `
        <table>
            <thead>
                <tr><th>Nº</th><th>Nombre</th><th>Tipo</th><th>Cinta</th></tr>
            </thead>
            <tbody>${filas}</tbody>
        </table>`;
}

/* --- Buscar por nombre --- */
document.getElementById('form-nombre').addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    const nombre = document.getElementById('input-nombre').value.trim();
    if (!nombre) return;

    resultadoEl.innerHTML = '';
    mostrarEstado('Buscando programa en la aplicación legada… (puede tardar ~20 s)', 'info');
    setCargando(btn, true);
    try {
        const res = await fetch('/api/programas?nombre=' + encodeURIComponent(nombre));
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Error desconocido');
        resultadoEl.innerHTML = renderPrograma(data);
        mostrarEstado('Programa encontrado.', 'ok');
    } catch (err) {
        mostrarEstado('Error: ' + err.message, 'error');
    } finally {
        setCargando(btn, false);
    }
});

/* --- Listar por cinta --- */
document.getElementById('form-cinta').addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    const cinta = document.getElementById('input-cinta').value.trim();
    if (!cinta) return;

    resultadoEl.innerHTML = '';
    mostrarEstado('Leyendo todos los programas y filtrando por cinta… (puede tardar bastante)', 'info');
    setCargando(btn, true);
    try {
        const res = await fetch('/api/cintas/' + encodeURIComponent(cinta) + '/programas');
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Error desconocido');
        if (!data.length) {
            mostrarEstado('No hay programas en la cinta "' + cinta + '".', 'info');
        } else {
            resultadoEl.innerHTML = renderTabla(data);
            mostrarEstado('Encontrados ' + data.length + ' programas en la cinta "' + cinta + '".', 'ok');
        }
    } catch (err) {
        mostrarEstado('Error: ' + err.message, 'error');
    } finally {
        setCargando(btn, false);
    }
});

document.getElementById('btn-refrescar-registros').addEventListener('click', cargarNumeroRegistros);

document.addEventListener('DOMContentLoaded', cargarNumeroRegistros);