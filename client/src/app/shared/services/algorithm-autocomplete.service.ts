import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, from } from 'rxjs';
import { PcComponents } from 'src/app/shared/models/pc-components-model';
import { ComponentType } from 'src/app/shared/models/component-type.model';

/**
 * @author Filippo Casarosa
 * Service for automatic PC configuration algorithm
 */
@Injectable({ providedIn: 'root' })
export class AlgorithmAutocompleteService {
  private readonly apiUrl = 'http://localhost:8080';

  constructor(
    private readonly http: HttpClient
  ) { }

  /**
   * Selects a random component from the array.
   * Uses Math.random() which is sufficient for non-cryptographic purposes.
   * @param components Array of components to choose from
   * @returns A randomly selected component
   */
  private selectRandomComponent(components: PcComponents[]): PcComponents {
    // Math.random() is safe here: selecting PC components, not generating security tokens
    const randomIndex = Math.floor(Math.random() * components.length);
    return components[randomIndex];
  }

  /**
   * Recursively creates a random PC setup
   * @param compatibleComponents Components compatible with previous selection
   * @param setup Current setup being built
   * @returns Promise with complete setup
   */
  private async randomCreator(
    compatibleComponents?: PcComponents[],
    setup?: PcComponents[]
  ): Promise<PcComponents[]> {
    // Initialize setup if undefined
    if (setup === undefined) {
      setup = [];

      // Fetch component types and initial components
      const types = await this.http.get<ComponentType[]>(`${this.apiUrl}/componentTypes`).toPromise();
      const allComponents = await this.http.get<PcComponents[]>(`${this.apiUrl}/components`).toPromise();

      if (types && types.length > 0 && allComponents) {
        const currentType = types[0];
        const filteredComponents = allComponents.filter(
          component => component.componentFamily.type.id === currentType.id
        );
        return this.innerCycle(filteredComponents, setup);
      }
      return setup;
    }

    // No more compatible components, return complete setup
    if (!compatibleComponents || compatibleComponents.length === 0) {
      return setup;
    }

    // Continue building setup
    return this.innerCycle(compatibleComponents, setup);
  }

  /**
   * Inner cycle that selects random component and fetches compatible ones
   * @param filteredComponents Available components to choose from
   * @param setup Current setup
   * @returns Promise with updated setup
   */
  private async innerCycle(
    filteredComponents: PcComponents[],
    setup: PcComponents[]
  ): Promise<PcComponents[]> {
    const chosenComponent = this.selectRandomComponent(filteredComponents);
    setup.push(chosenComponent);

    // Fetch compatible components for next iteration
    const compatibleComponents = await this.http
      .get<PcComponents[]>(
        `${this.apiUrl}/compatibilityConstraints/getByComponentId/${chosenComponent.id}`
      )
      .toPromise();

    return this.randomCreator(compatibleComponents, setup);
  }

  /**
   * Creates a random PC setup
   * @returns Observable with complete setup
   */
  public createRandomSetup(): Observable<PcComponents[]> {
    return from(this.randomCreator());
  }

  /**
   * Calculates total price of setup
   * @param setup PC components setup
   * @returns Total price
   */
  public calculateTotalPrice(setup: PcComponents[]): number {
    return setup.reduce((acc, component) => acc + component.price, 0);
  }

  /**
   * Calculates total power consumption
   * @param setup PC components setup
   * @returns Total power in Watts
   */
  public calculateTotalPower(setup: PcComponents[]): number {
    return setup.reduce((acc, component) => acc + (component.power || 0), 0);
  }
}
