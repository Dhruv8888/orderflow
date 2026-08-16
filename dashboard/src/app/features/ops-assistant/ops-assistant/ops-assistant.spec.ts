import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OpsAssistant } from './ops-assistant';

describe('OpsAssistant', () => {
  let component: OpsAssistant;
  let fixture: ComponentFixture<OpsAssistant>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OpsAssistant],
    }).compileComponents();

    fixture = TestBed.createComponent(OpsAssistant);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
