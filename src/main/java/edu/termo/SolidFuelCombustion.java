package edu.termo;

import java.util.HashMap;

import static edu.termo.KeyNames.*;

/**
 * Class computes heat of combustion, caloric value, required oxygen and exhaust composition from combustion of solid fuel.
 */
public class SolidFuelCombustion extends CombustionProcess {

    /**
     * @param elements hash map with elements share of solid fuel
     * @param lambda   air fuel ratio
     * @param x        absolute air humidity [(kg of H20)/(kg of fuel)
     */
    public SolidFuelCombustion(HashMap<String, Double> elements, double lambda, double x) {
        super(x, lambda);
        this.elements = elements;
        this.x = x;
        this.lambda = lambda;
    }


    public HashMap<String, Double> getFumesComposition() {
        getHeatingValue();

        double Ot = V_MOL * (elements.get(CARBON) / 12 + elements.get(HYDROGEN) / 4 + elements.get(SULFUR) / 32 - elements.get(OXYGEN) / 32); /*tlen teoretyczny*/
        double Oc = Ot * lambda; /*tlen całkowity*/

        double V0 = 100.0 / 21.0 * Ot;
        double V_wilg = (1.0 + 1.61 * x) * lambda * V0; /* zapotrzebowanie na powietrze wilgotne*/
        double V = lambda * V0;  /* powietrze całkowite */

        HashMap<String, Double> fumes = new HashMap<String, Double>();

        double V_CO2 = V_MOL / 12.0 * elements.get(CARBON);
        double V_SO2 = V_MOL / 32.0 * elements.get(SULFUR);
        double V_H20 = V_MOL * (elements.get(WATER) / 18 + elements.get(HYDROGEN) / 2) + 1.61 * x * V;
        double V_N = V_MOL / 28.0 * elements.get(NITROGEN) + 0.79 * lambda * V0;
        double V_O2 = Oc - Ot;
        double V_spalin = V_CO2 + V_SO2 + V_O2 + V_N;
        double V_spalin_wilg = V_spalin + V_H20;

        fumes.put(CO2, V_CO2);
        fumes.put(WATER, V_H20);
        fumes.put(NITROGEN, V_N);
        fumes.put(OXYGEN, V_O2);
        fumes.put(SO2, V_SO2);
        fumes.put(KeyNames.DRY_FUMES, V_spalin);
        fumes.put(KeyNames.WET_FUMES, V_spalin_wilg);

        /* Obliczanie składu i objętości paliwa testowane na zadaniu 2.6.11 */

        System.out.printf("Wartość opałowa: %f kJ/kg\nCiepło spalania: %f kJ/kg\n", Qi, Qs);
        System.out.printf("Zapotrzebowanie na powietrze:\n\tsuche: %f m^3/kg" +
                "\n\tmokre: %f m^3/kg\n", V0, V);
        System.out.printf("Udział procentowy spalin:\n" +
                "\tCO2 = %f %%\n" +
                "\tSO2 = %f %%\n" +
                "\tH20 = %f %%\n" +
                "\tN   = %f %%\n" +
                "\tO2  = %f %%\n",
                V_CO2 / V_spalin_wilg * 100.0, V_SO2 / V_spalin_wilg * 100.0, V_H20 / V_spalin_wilg * 100.0, V_N / V_spalin_wilg * 100.0, V_O2 / V_spalin_wilg * 100.0);
        System.out.printf("Objętość spalin:\n" +
                "\tsuchych: %f m^3/(kg paliwa)\n" +
                "\twilgotnych: %f m^3/(kg paliwa)\n", V_spalin, V_spalin_wilg);

        return fumes;
    }

    /* Obliczenie Qi i Qs testowane na zadaniu 2.6.1 ze skryptu*/
    @Override
    public double getHeatOfCombustion() {
        return Qi == null ? Qi = 34080 * elements.get(CARBON) + 142770 * (elements.get(HYDROGEN) - elements.get(OXYGEN) / 8)
                + 9290 * elements.get(SULFUR) - 2500 * (elements.get(WATER) + 9 * elements.get(HYDROGEN)) : Qi;
    }

    @Override
    public double getHeatingValue() {
        if (Qs == null) {
            double w = elements.get(WATER) + 9 * elements.get(HYDROGEN);

            Qs = (Qi == null ? getHeatOfCombustion() : Qi) + r0 * w;
        }

        return Qs;
    }
}