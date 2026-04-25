package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/eliel-ontiveros/gin-mongo/internal/config"
	"github.com/eliel-ontiveros/gin-mongo/internal/db"
	"github.com/eliel-ontiveros/gin-mongo/internal/handler"
	"github.com/eliel-ontiveros/gin-mongo/internal/repository"
	"github.com/eliel-ontiveros/gin-mongo/internal/service"
	"github.com/gin-gonic/gin"
)

func main() {
	cfg := config.Load()

	clients := db.Connect(cfg)
	defer db.Disconnect(clients)

	primaryDB := clients.PrimaryClient.Database(clients.PrimaryDB)
	secondaryDB := clients.SecondaryClient.Database(clients.SecondaryDB)

	primaryRepo := repository.NewPrimaryRepository(primaryDB)
	secondaryRepo := repository.NewSecondaryRepository(secondaryDB)

	recordSvc := service.NewRecordService(primaryRepo, secondaryRepo)

	recordH := handler.NewRecordHandler(recordSvc)
	seedH := handler.NewSeedHandler(recordSvc)

	router := gin.Default()

	api := router.Group("/api")
	{
		api.POST("/records", recordH.CreateRecord)
		api.GET("/records", recordH.GetRecords)
		api.POST("/seed", seedH.Seed)
	}

	srv := &http.Server{
		Addr:    ":" + cfg.Port,
		Handler: router,
	}

	go func() {
		log.Printf("server starting on port %s", cfg.Port)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("server error: %v", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	log.Println("shutting down server...")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := srv.Shutdown(ctx); err != nil {
		log.Fatalf("server forced to shutdown: %v", err)
	}
	log.Println("server exited")
}
