import json, os, collections

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EN = os.path.join(ROOT, "src/main/resources/assets/terrafabrica/lang/en_us.json")

en = json.load(open(EN, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)

translations = {
    "block.terrafabrica.carbon_fiber_plant": "Carbon Fiber Plant",
    "block.terrafabrica.enrichment_plant": "Uranium Enrichment Plant",
    "block.terrafabrica.ev_charger": "EV Charging Station",
    "block.terrafabrica.fusion_reactor": "Fusion Reactor",
    "block.terrafabrica.microchip_fab": "Microchip Fab",
    "block.terrafabrica.reactor_core": "Reactor Core",
    "block.terrafabrica.satellite_launchpad": "Satellite Launchpad",
    "block.terrafabrica.semiconductor_furnace": "Semiconductor Furnace",
    "block.terrafabrica.telescope": "Telescope",
    "block.terrafabrica.transistor_assembler": "Transistor Assembler",
    "item.terrafabrica.ai_core": "AI Core",
    "item.terrafabrica.carbon_fiber": "Carbon Fiber",
    "item.terrafabrica.deuterium": "Deuterium",
    "item.terrafabrica.drone": "Drone",
    "item.terrafabrica.ev_battery": "Traction Battery",
    "item.terrafabrica.fusion_fuel": "Fusion Fuel",
    "item.terrafabrica.gallium_arsenide": "Gallium Arsenide",
    "item.terrafabrica.germanium": "Germanium",
    "item.terrafabrica.gold_ingot": "Gold Ingot",
    "item.terrafabrica.indium": "Indium",
    "item.terrafabrica.lithium_ingot": "Lithium Ingot",
    "item.terrafabrica.nuclear_fuel_rod": "Nuclear Fuel Rod",
    "item.terrafabrica.photovoltaic_cell": "Photovoltaic Cell",
    "item.terrafabrica.quantum_computer": "Quantum Computer",
    "item.terrafabrica.reactor_shield": "Reactor Shield",
    "item.terrafabrica.robot": "Robot",
    "item.terrafabrica.satellite_part": "Satellite Part",
    "item.terrafabrica.silica": "Silica",
    "item.terrafabrica.silicon": "Silicon",
    "item.terrafabrica.solar_panel": "Solar Panel",
    "item.terrafabrica.superconductor": "Superconductor",
    "item.terrafabrica.tritium": "Tritium",
    "item.terrafabrica.zircon": "Zircon",
    "research.terrafabrica.aerospace_alloy": "Aerospace Alloy",
    "research.terrafabrica.aerospace_alloy.desc": "Super-strong alloys for aircraft.",
    "research.terrafabrica.carbon_fiber": "Carbon Fiber",
    "research.terrafabrica.carbon_fiber.desc": "Lightweight, strong composite materials.",
    "research.terrafabrica.enrichment": "Enrichment",
    "research.terrafabrica.enrichment.desc": "Increasing the concentration of U-235.",
    "research.terrafabrica.ev": "Electric Vehicle",
    "research.terrafabrica.ev.desc": "Battery-electric car.",
    "research.terrafabrica.fusion_reactor": "Fusion Reactor",
    "research.terrafabrica.fusion_reactor.desc": "Controlled fusion of deuterium and tritium.",
    "research.terrafabrica.microchip": "Microchip",
    "research.terrafabrica.microchip.desc": "Integrated circuits and microelectronics.",
    "research.terrafabrica.nuclear_reactor": "Nuclear Reactor",
    "research.terrafabrica.nuclear_reactor.desc": "Controlled fission of uranium nuclei.",
    "research.terrafabrica.satellite": "Satellite",
    "research.terrafabrica.satellite.desc": "Launching payloads into orbit.",
    "research.terrafabrica.semiconductor": "Semiconductors",
    "research.terrafabrica.semiconductor.desc": "Producing pure silicon and semiconductor materials.",
    "research.terrafabrica.transistor": "Transistor",
    "research.terrafabrica.transistor.desc": "Manufacturing precision semiconductor devices.",
    "research.terrafabrica.yellowcake_processing": "Yellowcake Processing",
    "research.terrafabrica.yellowcake_processing.desc": "Processing uranium ore into yellowcake.",
}

added = 0
for k, v in translations.items():
    if k not in en:
        en[k] = v
        added += 1

with open(EN, "w", encoding="utf-8") as f:
    json.dump(en, f, ensure_ascii=False, indent=2)
    f.write("\n")

print("added:", added, "total en keys:", len(en))
