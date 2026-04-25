package handler

import (
	"net/http"

	"github.com/eliel-ontiveros/gin-mongo/internal/service"
	"github.com/gin-gonic/gin"
)

type RecordHandler struct {
	svc *service.RecordService
}

func NewRecordHandler(svc *service.RecordService) *RecordHandler {
	return &RecordHandler{svc: svc}
}

type createRecordRequest struct {
	Name  string `json:"name" binding:"required"`
	Value string `json:"value" binding:"required"`
}

func (h *RecordHandler) CreateRecord(c *gin.Context) {
	tenant := c.GetHeader("X-Tenant-ID")
	if err := h.svc.ValidateTenant(tenant); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	var req createRecordRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	result, err := h.svc.CreateRecord(c.Request.Context(), tenant, req.Name, req.Value)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, result)
}

func (h *RecordHandler) GetRecords(c *gin.Context) {
	tenant := c.GetHeader("X-Tenant-ID")
	if err := h.svc.ValidateTenant(tenant); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	result, err := h.svc.GetAllRecords(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, result)
}
