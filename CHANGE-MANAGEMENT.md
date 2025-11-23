# Proceso Formal de Change Management

## Objetivo
Asegurar que todo cambio sea controlado, probado, aprobado y trazable de extremo a extremo, minimizando riesgos en producción.

## Alcance
Aplica a todos los microservicios del repositorio y a la infraestructura de CI/CD/observabilidad.

## Roles
- Owner técnico: valida impacto arquitectónico y riesgos.
- Equipo de desarrollo: implementa cambios y pruebas.
- Revisión (peer review): aprueba PRs.
- DevOps: gestiona despliegues y rollback.

## Fuente de cambio
- Issue o ticket con descripción, alcance, riesgos, criterios de aceptación y plan de pruebas.

## Flujo
1. Creación de rama feature/fix/chore desde `main`.
2. Desarrollo siguiendo convenciones de commits (Conventional Commits recomendado).
3. Pruebas locales y actualización de documentación.
4. Pull Request hacia `main`:
   - Descripción del cambio, checklist de pruebas y riesgos.
   - Requiere al menos una aprobación.
5. CI ejecuta:
   - Build + pruebas unitarias/integración.
   - Cobertura Jacoco.
   - Seguridad/performance (opcional por `workflow_dispatch`).
6. Merge a `main` tras aprobación.
7. Generación de Release Notes (automática con Release Drafter) y publicación de release etiquetado (workflow de releases).
8. Despliegue al entorno objetivo y monitoreo post-deploy.

## Criterios de entrada
- Issue/ticket referenciado en PR.
- Pruebas pasan y cobertura razonable.
- Documentación actualizada.

## Criterios de salida
- Release publicado con notas y tag.
- Monitoreo sin alertas críticas por 24h.

## Trazabilidad
- PR vincula commits, issues y release.
- Release registra artefactos y notas generadas.

## Gestión de riesgo
- Evaluación de impacto y plan de rollback documentado.
- Feature flags donde aplique.

