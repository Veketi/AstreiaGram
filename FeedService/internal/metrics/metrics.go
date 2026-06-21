package metrics

import "github.com/prometheus/client_golang/prometheus"

var EventsProcessed = prometheus.NewCounterVec(
	prometheus.CounterOpts{
		Name: "feed_service_kafka_events_processed_total",
		Help: "Total de eventos post-created processados",
	},
	[]string{"status"},
)

func init() {
	prometheus.MustRegister(EventsProcessed)
}

