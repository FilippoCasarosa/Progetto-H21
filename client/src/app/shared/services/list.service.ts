import { Injectable } from '@angular/core';
import { Observable, of, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ComponentType } from '../models/component-type.model';
import { PcComponents } from '../models/pc-components-model';
import { CompatibilityService } from './compatibility.service';
import { ComponentTypeService } from './component-type.service';
import { PcComponentsService } from './pc-components.service';

/**
 * @author Filippo Casarosa
 * @author Andrei Blindu
 */
@Injectable({ providedIn: 'root' })
export class ListService {
  private list: PcComponents[];
  private componentTypeList: ComponentType[];
  private current: number;
  private totalPrice: number;
  private totalPower: number;
  private powerSupplied: number;
  public setupState$: Subject<any> = new Subject();
  public initState$: Subject<any> = new Subject();

  constructor(
    private readonly componentTypeService: ComponentTypeService,
    private readonly compatibilityService: CompatibilityService,
    private readonly pcComponentsService: PcComponentsService
  ) {
    this.initState$.next(false);
    this.initSetup();
    this.componentTypeList = [];
    this.current = 0;
    this.totalPrice = 0;
    this.totalPower = 0;
    this.powerSupplied = 0;
  }

  public getCurrent(): number {
    return this.current;
  }

  public getList(): PcComponents[] {
    return this.list;
  }

  public getTotalPrice(): number {
    return this.totalPrice;
  }

  public getTotalPower(): number {
    return this.totalPower;
  }

  public getPowerSupplied(): number {
    return this.powerSupplied;
  }

  private calculateTotalPrice(): void {
    this.totalPrice += this.list[this.list.length - 1].price;
  }

  private calculateTotalPower(): void {
    if (this.list[this.list.length - 1].componentFamily.type.sortOrder === this.componentTypeList.length) {
      this.powerSupplied = this.list[this.list.length - 1].power;
    } else {
      this.totalPower += this.list[this.list.length - 1].power;
    }
  }

  checkPowerSupplied(): boolean {
    const powerSurplus = 1.2;
    return this.powerSupplied < this.totalPower * powerSurplus;
  }

  public fetchComponentTypes(): Observable<ComponentType[]> {
    return of(this.componentTypeList);
  }

  public initSetup(): void {
    this.list = [];
    this.setupState$.next(true);
    this.componentTypeService.getComponentType().subscribe(data => {
      this.componentTypeList = data;
      this.initState$.next(true);
    });
  }

  public addComponent(component: PcComponents): void {
    this.list.push(component);
    this.calculateTotalPrice();
    this.calculateTotalPower();
    if (this.list.length === this.componentTypeList.length) {
      this.setupState$.next(false);
    }
    this.current = this.list.length;
    console.log('list.length:' + this.list.length);
  }

  public getComponents(typeId: number): Observable<PcComponents[]> {
    if (this.list.length === 0) {
      return this.fetchPcComponents(typeId);
    } else {
      return this.fetchCompatiblePcComponents();
    }
  }

  private fetchPcComponents(typeId: number): Observable<PcComponents[]> {
    return this.pcComponentsService.getPcComponentsByType(typeId).pipe(
      tap(() => console.log())
    );
  }

  private fetchCompatiblePcComponents(): Observable<PcComponents[]> {
    return this.compatibilityService
      .getComponentsByCompatibility(this.list[this.list.length - 1].id)
      .pipe(tap(() => console.log()));
  }

  public deleteLastComponent(): void {
    this.list.splice(this.list.length - 1);
    if (this.list.length === this.componentTypeList.length) {
      this.setupState$.next(false);
    }
    this.current = this.list.length;
  }

  /**
   * Selects a random component from the array.
   * Uses Math.random() which is sufficient for non-cryptographic purposes like component selection.
   * @param components Array of components to choose from
   * @returns A randomly selected component
   */
  private selectRandomComponent(components: PcComponents[]): PcComponents {
    // Math.random() is safe here: we're selecting PC components, not generating security tokens
    const randomIndex = Math.floor(Math.random() * components.length);
    return components[randomIndex];
  }

  public autoConfig(typeId: number): void {
    this.getComponents(typeId).subscribe(res => {
      const filteredPcComponents: PcComponents[] = res;
      const choosenComponent = this.selectRandomComponent(filteredPcComponents);
      this.addComponent(choosenComponent);
      console.log(this.getList());
      if (this.list.length !== this.componentTypeList.length) {
        this.autoConfig(choosenComponent.componentFamily.type.id);
      }
    });
  }
}
