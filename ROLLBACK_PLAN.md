# Planes de Rollback

## Objetivo
Restaurar el sistema a un estado estable previo ante incidentes, con mínima pérdida de servicio y datos.

## Estrategia general
- Releases versionados y etiquetados (`vX.Y.Z`).
- Artefactos empaquetados por release.
- Backups de BD y migraciones reversibles cuando sea posible.

## Procedimientos

### Aplicación (Microservicios)
1. Identificar release estable anterior (p. ej., `vX.Y.(Z-1)`).
2. Revertir despliegue a imagen/artefacto de ese tag.
3. Verificar health checks y endpoints clave.
4. Monitorear métricas y logs (Prometheus/Grafana, ELK).

### Base de datos
- Backup previo a migraciones.
- Scripts de down-migration si están disponibles.
- Si no es reversible, restaurar desde backup.

### Feature Flags
- Desactivar nuevas funcionalidades problemáticas para rollback lógico.

### Comunicación
- Notificar stakeholders: descripción del incidente, tiempo estimado, pasos ejecutados.

## Validación post-rollback
- Health OK y latencias normales.
- Métricas de error dentro de umbrales.
- Logs sin excepciones repetitivas.

## Prevención futura
- Fortalecer pruebas y canary releases.
- Alertas de early-warning.

