import React from "react";
import PanelRendimientoMongo from "../../components/admin/PanelRendimientoMongo.jsx";
import TablaCorrelacionAmbiental from "../../components/admin/TablaCorrelacionAmbiental.jsx";

export default function EstadisticasAdminView() {
  return (
    <div>
      <h2 className="mb-4 text-dark">Estadísticas Administrativas</h2>
      <PanelRendimientoMongo />
      <div className="row">
        <div className="col-12">
          <TablaCorrelacionAmbiental />
        </div>
      </div>
    </div>
  );
}