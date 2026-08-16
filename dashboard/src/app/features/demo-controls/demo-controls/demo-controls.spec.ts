import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DemoControls } from './demo-controls';

describe('DemoControls', () => {
  let component: DemoControls;
  let fixture: ComponentFixture<DemoControls>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DemoControls],
    }).compileComponents();

    fixture = TestBed.createComponent(DemoControls);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
