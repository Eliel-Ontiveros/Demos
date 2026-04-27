package handler

import (
	"net/http"

	"github.com/eliel-ontiveros/gin-mongo/internal/service"
	"github.com/gin-gonic/gin"
)

type SeedHandler struct {
	svc *service.RecordService
}

func NewSeedHandler(svc *service.RecordService) *SeedHandler {
	return &SeedHandler{svc: svc}
}

func (h *SeedHandler) Seed(c *gin.Context) {
	result, err := h.svc.Seed(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, result)
}
