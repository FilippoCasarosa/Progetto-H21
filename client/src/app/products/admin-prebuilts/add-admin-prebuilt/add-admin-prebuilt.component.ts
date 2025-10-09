import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { PrebuiltService } from 'src/app/shared/services/prebuilt.service';
import { Prebuilt } from 'src/app/shared/models/prebuilt.model';

/**
 * @author Filippo Casarosa
 * @author Andrei Blindu
 */
@Component({
  selector: 'app-add-admin-prebuilt',
  templateUrl: './add-admin-prebuilt.component.html',
  styleUrls: ['./add-admin-prebuilt.component.scss']
})
export class AddAdminPrebuiltComponent implements OnInit {
  form: FormGroup;
  newPrebuilt: Prebuilt = new Prebuilt();
  selectedFile = null;
  saveComplete = false;

  constructor(private readonly prebuiltService: PrebuiltService) { }

  ngOnInit(): void {
    this.form = new FormGroup({
      name: new FormControl(''),
      use: new FormControl(''),
      price: new FormControl('')
    });
  }

  // <-- Add this method
  upload(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }


  onAddPrebuilt() {
    if (this.form.invalid) return;

    this.newPrebuilt.name = this.form.value.name;
    this.newPrebuilt.usage = this.form.value.use;
    this.newPrebuilt.totalPrice = this.form.value.price;

    this.prebuiltService.save(this.newPrebuilt, this.selectedFile).subscribe(() => {
      this.saveComplete = true;
      this.form.reset();
      this.selectedFile = null;
    });
  }
}
