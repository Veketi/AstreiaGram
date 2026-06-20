package config

import "os"

type Config struct {
	RedisAddr string
	KafkaBroker string
	UserServiceUrl string
	PostClientUrl string
}

func Load() *Config {
	return &Config{
		RedisAddr: getEnv(
			"REDIS_ADDR",
			"localhost:6379",
		),
		KafkaBroker: getEnv(
			"KAFKA_BROKER",
			"localhost:9092",
		),
		UserServiceUrl: getEnv(
			"USER_SERVICE_URL",
			"http://localhost:8083",
		),
		PostClientUrl: getEnv(
			"POST_SERVICE_URL",
			"http://localhost:8081",
		),
	}
}

func getEnv(
	key string,
	defaultValue string,
) string {
	 value := os.Getenv(key)

	 if value == "" {
		 return defaultValue
	 }

	 return value
}
