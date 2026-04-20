{{/*
Selector labels for notes-service — used in selector.matchLabels and pod template labels.
These must never change after the first deploy (immutable in Kubernetes).
*/}}
{{- define "notes-service.selectorLabels" -}}
app.kubernetes.io/name: {{ .Values.service.name }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Full labels for notes-service — used in metadata.labels of every resource.
Includes chart version for observability. Builds on top of selectorLabels.
*/}}
{{- define "notes-service.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- include "notes-service.selectorLabels" . | nindent 0 }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}
