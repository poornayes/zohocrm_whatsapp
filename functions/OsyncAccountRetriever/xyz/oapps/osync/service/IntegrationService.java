package xyz.oapps.osync.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import xyz.oapps.osync.OsyncException;
import xyz.oapps.osync.SyncHandler;
import xyz.oapps.osync.SyncHandlerRepo;
import xyz.oapps.osync.api.OsyncConstants.IntegrationStatus;
import xyz.oapps.osync.api.RequestController;
import xyz.oapps.osync.entity.FieldMapEntity;
import xyz.oapps.osync.entity.IntegrationPropsEntity;
import xyz.oapps.osync.entity.IntegrationStatusEntity;
import xyz.oapps.osync.entity.ModuleInfoEntity;
import xyz.oapps.osync.entity.ServiceInfoEntity;
import xyz.oapps.osync.entity.SyncLogEntity;
import xyz.oapps.osync.entity.UniqueValuesMapEntity;
import xyz.oapps.osync.fields.Record;
import xyz.oapps.osync.fields.RecordSet;
import xyz.oapps.osync.repo.AccountInfoRepository;
import xyz.oapps.osync.repo.FieldMapRepository;
import xyz.oapps.osync.repo.IntegrationPropsRepository;
import xyz.oapps.osync.repo.IntegrationStatusRepository;
import xyz.oapps.osync.repo.ModuleInfoRepository;
import xyz.oapps.osync.repo.ServiceInfoRepository;
import xyz.oapps.osync.repo.SyncLogEntityRepo;
import xyz.oapps.osync.repo.UniqueValuesMapRepo;
import xyz.oapps.osync.util.CommonUtil;

public class IntegrationService {

	private static final String DUPLICATE = "duplicate";
	private static final String short_name = "int_ser";

	SyncLogEntityRepo logRepo = new SyncLogEntityRepo();

	IntegrationPropsRepository intPropsRepo = new IntegrationPropsRepository();

	IntegrationStatusRepository intStatusRepo = new IntegrationStatusRepository();

	ModuleInfoRepository moduleInfoRepo = new ModuleInfoRepository();

	ServiceInfoRepository serviceInfoRepo = new ServiceInfoRepository();

	FieldMapRepository fieldMapRepo = new FieldMapRepository();

	UniqueValuesMapRepo uvMapRepo = new UniqueValuesMapRepo();

	AccountInfoRepository accountRepo = new AccountInfoRepository();

	boolean forcePause = false;

	private static final Logger log = Logger.getLogger(IntegrationService.class.getName());
	

	public SyncLogEntity sync2(String osyncId, String integId, boolean forceSync) throws Exception {
		
		SyncLogEntity logEntity = new SyncLogEntity();
		Long syncStartTime = System.currentTimeMillis();
		IntegrationStatusEntity status = intStatusRepo.findById(integId);
		IntegrationPropsEntity intProps = null;
		try {
			currentContext.set(RequestController.getUUID().substring(0, 10) + "-" + integId);
			intProps = intPropsRepo.findById(integId);
			
			
			CommonUtil.logOsyncInfo(short_name,":: intProps >>" + intProps);
			
			if (intProps == null) {
				throw new OsyncException(OsyncException.Code.SYNC_INTEG_ID_NOT_PRESENT, "Integration ID Not present");
			}
			int syncStatus = intProps.getSyncStatus();
			
			CommonUtil.logOsyncInfo(short_name,":: sync2 ::syncStatus >>"+ syncStatus);
			
			if (syncStatus != 1) {
				CommonUtil.logOsyncInfo(short_name,"Sync paused :" + osyncId + ":: Integ ID: " + integId);
				throw new OsyncException(OsyncException.Code.SYNC_PAUSED);
			}
			
			CommonUtil.logOsyncInfo(short_name,":: sync2 :: syncStatus 1 >>"+ syncStatus);
			
			Long startTime = -1l;
			Long endTime = System.currentTimeMillis();
			if (status != null) {
				
				CommonUtil.logOsyncInfo(short_name,":: sync2 ::syncStatus 2 >>"+ status);
				
				if (status.getStatus().equals(IntegrationStatus.COMPLETE)) {
					
					CommonUtil.logOsyncInfo(short_name,":: sync2 ::status.getStatus()  >>"+ status.getStatus());
					
					
					startTime = status.getEndTime().getTime();

					status.setPrevStartTime(status.getStartTime());
					status.setPrevEndTime(status.getEndTime());

					status.setStartTime(status.getEndTime());
					status.setEndTime(new Date(endTime));

					status.setStatus(IntegrationStatus.RUNNING);

					intStatusRepo.update(status);

				} else if (status.getStatus().equals(IntegrationStatus.RUNNING)) {
					
					
					CommonUtil.logOsyncInfo(short_name,":: sync2 ::New sync initiated, but old sync still in progress  >>"+ status.getStatus());
					
					logEntity.setStatus(IntegrationStatus.NOT_STARTED);
					throw new OsyncException(OsyncException.Code.SYNC_IN_PROGRESS, "Sync in progress");
				} else if (status.getStatus().equals(IntegrationStatus.ERROR)) {
					
					CommonUtil.logOsyncInfo(short_name,":: sync2 ::Old sync ended in error. Please check the status.  >>"+ status.getStatus());
					
					logEntity.setStatus(IntegrationStatus.NOT_STARTED);
					if (forceSync == false) {
						log.severe("Old sync ended in error. Please check the status." + "for :: osyncid:integid"
								+ osyncId + ":" + integId + intProps + status);
						throw new OsyncException(OsyncException.Code.SYNC_OLD_SYNC_ERROR,
								"Old sync ended in error. Please check the status.");
					}
				} else if (status.getStatus().equals(IntegrationStatus.RESTART)) {

					CommonUtil.logOsyncInfo(short_name,":: sync2 ::Restarting from old end  >>"+ status.getStatus());
					
					logEntity.setStatus(IntegrationStatus.RESTART);
					status.setStatus(IntegrationStatus.RUNNING);
					intStatusRepo.update(status);
				} else {
					CommonUtil.logOsyncInfo(short_name,":: sync2 ::Sync2 status else block::::  >>"+ status.getStatus());
					
					if (forceSync == false) {
						
						CommonUtil.logOsyncInfo(short_name,":: sync2 ::Sync2 forceSync == false::::  >>"+ forceSync);
						
						throw new OsyncException(OsyncException.Code.SYNC_INVALID_STATUS,
								"Invalid status encountered " + integId + intProps + status);
					}
				}

			} else {
				
				CommonUtil.logOsyncInfo(short_name,":: sync2 ::Sync2 status not null ::::  >>"+ status);
				
				
				status = new IntegrationStatusEntity();
				status.setStatus(IntegrationStatus.RUNNING);
				status.setIntegId(integId);
				status.setOsyncId(osyncId);
				status.setEndTime(new Date(endTime));
				status = intStatusRepo.save(status);
			}

			ServiceInfoEntity serviceA = getServiceInfo(intProps.getLeftServiceId());
			CommonUtil.logOsyncInfo(short_name,":: sync2 ::serviceA ::  getServiceName::"+ serviceA.getName());
			CommonUtil.logOsyncInfo(short_name,":: sync2 ::serviceA ::  getServiceId::"+ serviceA.getServiceId());
			
			ModuleInfoEntity moduleA = getModuleInfo(serviceA, osyncId, integId, true, intProps.getLeftModuleId());
			CommonUtil.logOsyncInfo(short_name,":: sync2 ::moduleA ::  getModuleName::"+ moduleA.getName());
			CommonUtil.logOsyncInfo(short_name,":: sync2 ::moduleA ::  getModuleId::"+ moduleA.getModuleId());

			CommonUtil.logOsyncInfo(short_name,":: sync2 ::Getting data for osyncId 1::  getLeftModuleId::"+ intProps.getLeftModuleId());

			ServiceInfoEntity serviceB = getServiceInfo(intProps.getRightServiceId());
			CommonUtil.logOsyncInfo(short_name,":: sync2 ::serviceB ::  getServiceName::"+ serviceB.getName());
			CommonUtil.logOsyncInfo(short_name,":: sync2 ::serviceB ::  getServiceId::"+ serviceB.getServiceId());
			
			ModuleInfoEntity moduleB = getModuleInfo(serviceB, osyncId, integId, false, intProps.getRightModuleId());
			CommonUtil.logOsyncInfo(short_name,":: sync2 ::moduleB ::  getModuleName::"+ moduleB.getName());
			CommonUtil.logOsyncInfo(short_name,":: sync2 ::moduleB ::  getModuleId::"+ moduleB.getModuleId());

			CommonUtil.logOsyncInfo(short_name,":: sync2 ::Getting data for osyncId 2::  getLeftModuleId::"+ intProps.getLeftModuleId());

			List<FieldMapEntity> fieldMaps = fieldMapRepo.findAllByIntegId(integId);

			SyncHandler controllerA = getSyncControllerInstance(serviceA, moduleA, osyncId, integId, true);
			SyncHandler controllerB = getSyncControllerInstance(serviceB, moduleB, osyncId, integId, false);
			
			// controller.fetchUpdatedRecords(osyncId, lastSyncTime)
			boolean isLeft = true;

			logEntity.setLeftService(serviceA.getName());
			logEntity.setRightService(serviceB.getName());
			logEntity.setLeftModule(moduleA.getName());
			logEntity.setRightModule(moduleB.getName());

			int direction = intProps.getDirection();
			CommonUtil.logOsyncInfo(short_name,":: sync2 :: direction >>>>"+ direction);


			/**
			 * 1 - Left - Right 2 - Right - Left 3 - Both
			 */
			checkIfPaused(osyncId, integId);
			if (direction == 1 || direction == 3) {
				CommonUtil.logOsyncInfo(short_name,":: sync2 ::Sync2 getting syncOneWay direction 1/3  ::"+ osyncId + ":" + integId+ ":" + direction);
				syncOneWay(intProps, controllerA, controllerB, startTime, endTime, serviceA, moduleA, serviceB, moduleB,
						fieldMaps, isLeft, logEntity, osyncId,"forward");
				CommonUtil.logOsyncInfo(short_name,"Sync2 getting syncOneWay direction 1/3::" + "for :: osyncid:integid" + osyncId + ":" + integId
						+ direction);
			}

			if (direction == 2 || direction == 3) {
				CommonUtil.logOsyncInfo(short_name,":: sync2 ::Sync2 getting syncOneWay SECOND direction 1/3  ::"+ osyncId + ":" + integId+ ":" + direction);
				syncOneWay(intProps, controllerB, controllerA, startTime, endTime, serviceB, moduleB, serviceA, moduleA,
						fieldMaps, !isLeft, logEntity, osyncId,"backward");
				CommonUtil.logOsyncInfo(short_name,"Sync2 getting syncOneWay direction 2/3::" + "for :: osyncid:integid" + osyncId + ":" + integId
						+ direction);
			}

			status.setStatus(IntegrationStatus.COMPLETE);
			CommonUtil.logOsyncInfo(short_name,":: sync2 ::syncOneWay  try in the last  ::"+"");
			intStatusRepo.update(status);

		} catch (OsyncException e) {
			CommonUtil.logOsyncInfo(short_name,":: sync2 ::OsyncException >>>>  ::"+e.getMessage());
			e.printStackTrace();
			if (e.getCode().equals(OsyncException.Code.INVOKER_AUTH_FAILED)) {
				CommonUtil.logOsyncInfo(short_name,"Sync2 AUTHENTICATION_FAILED::" + "for :: osyncid:integid" + osyncId + ":" + integId);
				status.setStatus(IntegrationStatus.AUTHENTICATION_FAILED);
				intStatusRepo.update(status);
//				intProps.setSyncStatus(2);
				intPropsRepo.update(intProps.getIntegId(), intProps);
				CommonUtil.doOsyncFailureProcess(osyncId, integId, "Authentication Failure");
			}
		} catch (Exception e) {
			CommonUtil.logOsyncInfo(short_name,":: sync2 ::Exception DATA >>>>  ::"+e.getMessage());
			e.printStackTrace();
			CommonUtil.logOsyncInfo(short_name,"Restarting from old end " + "for :: osyncid:integid" + osyncId + ":" + integId + intProps
					+ status);
			if (status != null) {
				status.setStatus(IntegrationStatus.ERROR);
				intStatusRepo.update(status);
				CommonUtil.doOsyncFailureProcess(osyncId, integId, "some technical glitch");
			}
			throw e;
		} finally {
			try {
				CommonUtil.logOsyncInfo(short_name,"Sync2 try in finally 1111: " + "for :: osyncid:integid" + osyncId + ":" + integId);
				if (forcePause) {
					logEntity.setStatus(IntegrationStatus.FORCEPAUSE);
				} else {
					logEntity.setStatus(IntegrationStatus.COMPLETE);
				}
				logEntity.setStartTime(new Date(syncStartTime));
				logEntity.setEndTime(new Date(System.currentTimeMillis()));
				logEntity.setIntegId(integId);
				logEntity.setOsyncId(osyncId);
				logRepo.save(logEntity);
				CommonUtil.logOsyncInfo(short_name,"Sync2 try in finally" + "for :: osyncid:integid" + osyncId + ":" + integId);
			} finally {
				CommonUtil.logOsyncInfo(short_name,"Sync2 finally" + "for :: osyncid:integid" + osyncId + ":" + integId);
				CommonUtil.logOsyncInfo(short_name,"Sync completed -- Log entity" + logEntity);
				currentContext.set(null);
			}
		}

		return logEntity;
	}

	private void syncOneWay(IntegrationPropsEntity intProps, SyncHandler controllerA, SyncHandler controllerB,
			Long startTime, Long endTime, ServiceInfoEntity serviceA, ModuleInfoEntity moduleA,
			ServiceInfoEntity serviceB, ModuleInfoEntity moduleB, List<FieldMapEntity> fieldMaps, boolean isLeft,
			SyncLogEntity logEntity, String osyncId , String generatedString) throws Exception {
		try {
			int startPage = 1;
			int totalRecords = 100;
			String integId = intProps.getIntegId();

			
			CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Sync started from >>"+new Date(startTime));
			CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Sync started endTime >>" +new Date(endTime));
			
			
			
			checkIfPaused(osyncId, integId);
			
			CommonUtil.logOsyncInfo(short_name,"Fetching records from Controller A -- Service A"+ serviceA.getName()+" :: Module A >>" + moduleA.getName() +" :: Service B >>"+ serviceB.getName()+ " :: Module B >>"+ moduleB.getName());

			RecordSet recordSetA = controllerA.fetchRecords(startPage, totalRecords, startTime);
			if (recordSetA == null || recordSetA.count() == 0) {
				CommonUtil.logOsyncInfo(short_name,"No records fetched. Hence returning.");
				return;
			}
			checkIfPaused(osyncId, integId);
			CommonUtil.logOsyncInfo(short_name,"Records fetched from " + serviceA.getName() + "Count >>" + recordSetA.count());
			recordSetA.fillUniqueValueMap(uvMapRepo, integId, isLeft);

			do {
				checkIfPaused(osyncId, integId);
//				CommonUtil.logOsyncInfo(short_name,"Sync start", "start", startPage, "totalRecords", totalRecords, "integId", integId, "isLeft",isLeft);
				
				CommonUtil.logOsyncInfo(short_name,":: syncOneWay :: Start >>"+ startPage +":: totalRecords >>"+ totalRecords +":: integId >>"+ integId +" :: isLeft >>"+ isLeft);
				
				RecordSet toCreate = RecordSet.init(serviceB.getName(), moduleB.getPrimaryColumn())
						.setSource(serviceA.getName()).setDestination(serviceB.getName())
						.setSourceModule(moduleA.getName()).setDestinationModule(moduleB.getName());
				
				RecordSet toUpdate = RecordSet.init(serviceB.getName(), moduleB.getPrimaryColumn())
						.setSource(serviceA.getName()).setDestination(serviceB.getName())
						.setSourceModule(moduleA.getName()).setDestinationModule(moduleB.getName());
				
				logEntity.addFetchCount(recordSetA.count(), isLeft);

				List<String> recordsToFetchB = new ArrayList<String>();
				HashMap<String, String> fetchByUniqueColumn = new HashMap<String, String>();
				for (Record record : recordSetA) {

					if (intProps.isSyncRecordsWithEmail() && moduleA.getEmailColumn() != null
							&& !moduleA.getEmailColumn().isEmpty()
							&& record.getValue(moduleA.getEmailColumn()) == null) {
						// do not sync the records without email
						logEntity.incrementSkippedCount(isLeft);
						continue;
					}

					boolean isUpdate = record.getMappedRecordUniqueValue() != null;

					if (isUpdate) {
						if (!DUPLICATE.equals(record.getMappedRecordUniqueValue())) {
							recordsToFetchB.add(record.getMappedRecordUniqueValue());
						} else {
							CommonUtil.logOsyncInfo(short_name,"Ignoring update for DUPLICATE Record"+ record.getUniqueValue());
						}
					} else {
						// create
						if (intProps.isLookupUniqueColumn()) {
							// lookup and then decide
							String uniqueValue = record.getValue(moduleA.getUniqueColumn()).toString();
							fetchByUniqueColumn.put(record.getUniqueValue(), uniqueValue);
						} else {
							// no need to lookup, add as new record
							Record nrecord = toCreate.createEmptyObject();
							try {
								compareAndFillRecord(nrecord, record, null, fieldMaps, isLeft);
								nrecord.setMappedRecordUniqueValue(record.getUniqueValue());
							} catch (OsyncException e) {
								if (e.getCode().equals(OsyncException.Code.FIELD_MAP_MANDATORY_VALUE_MISSING)) {
									CommonUtil.logOsyncInfo(short_name,"Mandatory field map value missing Record A >>"+ record + " :: Exception"+e.getMessage());
									toCreate.remove(nrecord.getUniqueValue());
									logEntity.addErrorsCount(1, !isLeft);
								}
							}
						}
					}
				}
				CommonUtil.logOsyncInfo(short_name,"Counts Record count to fetch from " + serviceB.getName()+"::"+ recordsToFetchB.size()+"Record count to check for unique columns"+ fetchByUniqueColumn.size());
				
				
				CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Record count to fetch from " + serviceB.getName()+"::"+recordsToFetchB.size());
				CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Record count to check for unique columns"+ fetchByUniqueColumn.size());
				
				if (recordsToFetchB.size() > 0) {
					// updates
					checkIfPaused(osyncId, integId);
					CommonUtil.logOsyncInfo(short_name,"Fetching matching records by ID from " + serviceB.getName()+ ":: Unique IDs"+ recordsToFetchB);
					
					CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Fetching matching records by ID from " + serviceB.getName() + "Unique IDs >>>>"+ recordsToFetchB);
					
					
					RecordSet recordSetB = controllerB.getMatchedRecordsById(recordsToFetchB);
					if (recordSetB == null) {
						// to avoid NPE
						recordSetB = RecordSet.init(serviceB.getName(), moduleB.getPrimaryColumn());
					}
					checkIfPaused(osyncId, integId);
					CommonUtil.logOsyncInfo(short_name,"Fetched matching records from " + serviceB.getName()+ ":: Count returned >>"+ recordSetB.count() + ":: Values returned"+ recordSetB.getUniqueIds());
					
					CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Fetched matching records from " + serviceB.getName() + "Count returned::: " + recordSetB.count() + " ::: Values returned ::::" + recordSetB.getUniqueIds());
					
					recordSetB.fillUniqueValueMap(uvMapRepo, integId, !isLeft);

					for (Record recordA : recordSetA) {
						Record recordB = recordSetB.find(recordA.getMappedRecordUniqueValue());
						if (recordB != null) {
							Record newRecord = toUpdate.add(recordA.getMappedRecordUniqueValue(), null);
							boolean hasAnyChange = compareAndFillRecord(newRecord, recordA, recordB, fieldMaps, isLeft);
							if (hasAnyChange) {
								// need to check conflict here
								// if the modified_time falls within the same range, we need to choose the
								// record based on conflict resolution.
								if (recordB.getModifiedTime() > startTime) {
									// both are modified
									if (intProps.getMasterService() != null
											&& intProps.getMasterService().equals(serviceB.getServiceId())) {
										logEntity.incrementConflictCount();
										toUpdate.remove(recordA.getMappedRecordUniqueValue());
										continue;
									}
								}
								newRecord.setMappedRecordUniqueValue(recordA.getUniqueValue());
							} else {
								// there is no change in the object, hence removing from the overall list
								logEntity.incrNoChangeCount(isLeft);
								toUpdate.remove(recordA.getMappedRecordUniqueValue());
							}

						}
					}
				}

				int totalCreateAfterMatch = 0;
				int totalUpdateAfterMatch = 0;

				if (fetchByUniqueColumn.size() > 0) {
					// creates
					checkIfPaused(osyncId, integId );
					CommonUtil.logOsyncInfo(short_name,"Fetching matching records by Unique Column from " + serviceB.getName()+" ::Unique Columns"+fetchByUniqueColumn);
					
					CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Fetching matching records by Unique Column from " + serviceB.getName() + "Unique Columns >>> "+fetchByUniqueColumn);
					RecordSet uniqueRecordSetB = controllerB
							.getMatchedRecordsByUniqueColumn(fetchByUniqueColumn.values());
					if (uniqueRecordSetB == null) {
						uniqueRecordSetB = RecordSet.init(serviceB.getName(), moduleB.getPrimaryColumn());
					}
					int count = markDuplicates(integId, uniqueRecordSetB, isLeft);
					CommonUtil.logOsyncInfo(short_name,"Fetched matching records by Unique Column from " + serviceB.getName()+" ::Count"+ uniqueRecordSetB.count() + ":: Unique IDs"+ uniqueRecordSetB.getUniqueIds());
					
					CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Fetched matching records by Unique Column from " + serviceB.getName() + "Count >>> " + uniqueRecordSetB.count()+ "::: Unique IDs >>>"+ uniqueRecordSetB.getUniqueIds());
					
					
					
					logEntity.setDuplicatesCount(count);
					uniqueRecordSetB.setUniqueColumnName(moduleB.getUniqueColumn());
					for (Entry<String, String> entry : fetchByUniqueColumn.entrySet()) {
						String id = entry.getKey();
						String uniqueValue = entry.getValue();
						Record recordA = recordSetA.find(id);
						Record recordB = uniqueRecordSetB.findByUniqueColumn(uniqueValue);
						if (recordB == null) {
							Record nrecord = toCreate.createEmptyObject();
							try {
								compareAndFillRecord(nrecord, recordA, null, fieldMaps, isLeft);
								nrecord.setMappedRecordUniqueValue(recordA.getUniqueValue());
								totalCreateAfterMatch++;
							} catch (OsyncException e) {
								if (e.getCode().equals(OsyncException.Code.FIELD_MAP_MANDATORY_VALUE_MISSING)) {
									CommonUtil.logOsyncInfo(short_name,"Mandatory field map value missing Record A"+ recordA+" ::Exception"+e.getMessage());
									
									CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Mandatory field map value missing Record A >>>>" + recordA + "::: Exception"+e.getMessage());
									
									toCreate.remove(nrecord.getUniqueValue());
									logEntity.addErrorsCount(1, !isLeft);
								}
							}
						} else if (recordB.isDuplicate()) {
							addUVMapping(intProps, recordA.getUniqueValue(), DUPLICATE, isLeft, logEntity);
						} else if (intProps.getMasterService() == null
								|| intProps.getMasterService().equals(serviceA.getServiceId())) {
							addUVMapping(intProps, recordA, recordB, moduleA.getUniqueColumn(), isLeft, logEntity);
							logEntity.incrementUniqueColumnMatch();
							Record newRecord = toUpdate.add(recordA.getMappedRecordUniqueValue(), null);
							compareAndFillRecord(newRecord, recordA, recordB, fieldMaps, isLeft);
							totalUpdateAfterMatch++;
						}
					}
					CommonUtil.logOsyncInfo(short_name,"Matched unique columns Total create after match>>"+ totalCreateAfterMatch+" :: Total update after match >>"+ totalUpdateAfterMatch);
					CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Matched unique columns , Total create after match >>>> " + totalCreateAfterMatch + " Total update after match >>>" + totalUpdateAfterMatch);
				}
				checkIfPaused(osyncId, integId);
				CommonUtil.logOsyncInfo(short_name,"Overall count to " + serviceB.getName()+" ::Create >> "+ toCreate.count()+" ::Update"+ toUpdate.count());
				
				CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Overall count to " + serviceB.getName()+ "Create >>>"+ toCreate.count()+ "::: Update >>>>" + toUpdate.count());
				
				
				if (toCreate.count() > 0) {
					CommonUtil.logOsyncInfo(short_name,"Records to be created "+ toCreate.count());
					
					CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Records to be created"+ toCreate.count());
					
					Collection<Record> allRecords = toCreate.getAllRecords();
					for (Record record : allRecords) {
						CommonUtil.logOsyncInfo(short_name,"Create" + record.getUniqueValue()+"::"+ record.getColumnValues());
					}
					HashMap<String, String> uvMap = controllerB.createNewRecords(toCreate, null);
					CommonUtil.logOsyncInfo(short_name,"Created records to " + serviceB.getName()+" ::Values >>"+ uvMap);
					
					CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Created records to " + serviceB.getName()+"Values >>>"+ uvMap);
					
					for (Entry<String, String> uvEntry : uvMap.entrySet()) {
						addUVMapping(intProps, uvEntry.getKey(), uvEntry.getValue(), isLeft, logEntity);
					}
					logEntity.addCreatedCount(uvMap.size(), !isLeft);
					logEntity.addErrorsCount(toCreate.count() - uvMap.size(), !isLeft);
				}
				if (toUpdate.count() > 0) {
					logEntity.addUpdatedCount(toUpdate.count(), !isLeft);
					CommonUtil.logOsyncInfo(short_name,"Records to be updated "+ toUpdate.count());
					
					CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Records to be updated"+  toUpdate.count());
					
					Collection<Record> allRecords = toUpdate.getAllRecords();
					for (Record record : allRecords) {
						CommonUtil.logOsyncInfo(short_name,"Update>>"+record.getUniqueValue()+"::"+ record.getColumnValues());
					}
					controllerB.updateRecords(toUpdate, null);
					CommonUtil.logOsyncInfo(short_name,"Updated records to " + serviceB.getName());
					
					CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Updated records to " + serviceB.getName());
				}

				startPage++;
				CommonUtil.logOsyncInfo(short_name,":: syncOneWay DECISION_MAKER ::recordSetA.count() >>>>> " + recordSetA.count()+ "totalRecords >>>"+totalRecords);
				if (recordSetA.count() == totalRecords) {
					recordSetA = controllerA.fetchRecords(startPage, totalRecords, startTime);
					recordSetA = recordSetA == null ? RecordSet.init(serviceA.getName(), moduleA.getPrimaryColumn())
							: recordSetA;
					recordSetA.fillUniqueValueMap(uvMapRepo, integId, isLeft);
				} else {
					CommonUtil.logOsyncInfo(short_name,"SyncOneWay complete");
					CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::Status >>>>>SyncOneWay complete");
					
					// we are done here
					break;
				}
				// TODO: Temp
			} while (recordSetA.count() > 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			CommonUtil.logOsyncInfo(short_name,"syncOneWay exception :::" + e);
			CommonUtil.logOsyncInfo(short_name,":: syncOneWay ::exception occured >>>>>" + e.getMessage());
			e.printStackTrace();
		}
	}

	private int markDuplicates(String integId, RecordSet uniqueRecordSetB, boolean isLeft) throws Exception {
		List<String> uniqueIds = uniqueRecordSetB.getUniqueIds();
		List<UniqueValuesMapEntity> uniqueValuesMap = null;
		if (isLeft) {
			uniqueValuesMap = uvMapRepo.findByIntegIdAndRightUniqueValueIn(integId, uniqueIds);
		} else {
			uniqueValuesMap = uvMapRepo.findByIntegIdAndLeftUniqueValueIn(integId, uniqueIds);
		}
		int duplicates = 0;
		if (uniqueValuesMap != null) {
			for (UniqueValuesMapEntity uniqueValuesMap2 : uniqueValuesMap) {
				Record find = uniqueRecordSetB
						.find(isLeft ? uniqueValuesMap2.getRightUniqueValue() : uniqueValuesMap2.getLeftUniqueValue());
				if (find != null) {
					find.setDuplicate(true);
					duplicates++;
				}
			}
		}
		return duplicates;
	}

	static ThreadLocal<String> currentContext = new ThreadLocal<String>();

	private void ilog(String message, Object... values) throws Exception {
		// int startPage, int totalRecords, Long integId, boolean isLeft, String name,
		StringBuilder logMessage = new StringBuilder("[SYNC_LOG] [ " + currentContext.get() + " ] ");
		try {
			logMessage.append(message).append(", ");
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					if (values[i] == null) {
						logMessage.append("NULL").append(": ");
					} else {
						logMessage.append(values[i]).append(": ");
					}
					i++;
					if (values.length > i) {
						if (values[i] == null) {
							logMessage.append("NULL").append(", ");
						} else {
							logMessage.append(values[i]).append(", ");
						}
					}
				}
			}
			CommonUtil.logOsyncInfo(short_name,logMessage.toString());
		} catch (Exception e) {
			log.log(Level.WARNING, "Error in formatting the log," + message + " {*}", values);
			log.log(Level.WARNING, logMessage.toString(), e);
		}
	}

	private void checkIfPaused(String osyncId, String integId ) throws Exception {
		try {
			IntegrationPropsEntity findTopByOsyncIdAndIntegId = intPropsRepo.findTopByOsyncIdAndIntegId(osyncId,
					integId);
			int sync_status = findTopByOsyncIdAndIntegId.getSyncStatus();
			if (sync_status == 2) {
				forcePause = true;
				CommonUtil.logOsyncInfo(short_name,":: sync2 :: :::: Paused error  ::FORCE_PAUSE_HAPPENED");
				throw new Exception("FORCE_PAUSE_HAPPENED::::" + integId);
			}
		} catch (Exception ex) {
			CommonUtil.logOsyncInfo(short_name,":: sync2 :: :::: Paused error > FORCE_PAUSE_HAPPENED  ::" + ex.getMessage());
			System.out.println("checkIfPaused exception ::: " + ex);
		}
	}

	public ModuleInfoEntity getModuleInfo(ServiceInfoEntity service, String osyncId, String integId, boolean isLeft,
			String moduleId) throws Exception {
		if (service.isDynamicModule()) {
			SyncHandler syncHandler = SyncHandlerRepo.getInstance(service, null, osyncId, integId, isLeft);
			List<ModuleInfoEntity> modules = syncHandler.getModules(osyncId, service.getServiceId());
			if (modules != null) {
				for (ModuleInfoEntity moduleInfoEntity : modules) {
					if (moduleInfoEntity.getModuleId().equals(moduleId)) {
						return moduleInfoEntity;
					}
				}
			}
			throw new Exception("Module not found");
		} else {
			return moduleInfoRepo.findByModuleId(moduleId);
		}
	}

	public ServiceInfoEntity getServiceInfo(String leftServiceId) throws Exception {
		ServiceInfoEntity findById = serviceInfoRepo.findByServiceId(leftServiceId);
		return findById;
	}

	private SyncHandler getSyncControllerInstance(ServiceInfoEntity service, ModuleInfoEntity module, String osyncId,
			String integId, boolean isLeft) {
		return SyncHandlerRepo.getInstance(service, module, osyncId, integId, isLeft);
	}

	private boolean addUVMapping(IntegrationPropsEntity intProps, String leftUniqueValue, String rightUniqueValue,
			boolean isLeftToRight, SyncLogEntity logEntity) throws Exception {
		if (leftUniqueValue == null || rightUniqueValue == null || "".equals(leftUniqueValue.trim())
				|| "".equals(rightUniqueValue.trim())) {
			return false;
		}
		UniqueValuesMapEntity uniqueValuesMap = new UniqueValuesMapEntity();
		uniqueValuesMap.setIntegId(intProps.getIntegId());
		uniqueValuesMap.setOsyncId(intProps.getOsyncId());
		if (isLeftToRight) {
			uniqueValuesMap.setLeftUniqueValue(leftUniqueValue);
			uniqueValuesMap.setRightUniqueValue(rightUniqueValue);
		} else {
			uniqueValuesMap.setRightUniqueValue(leftUniqueValue);
			uniqueValuesMap.setLeftUniqueValue(rightUniqueValue);
		}
		uvMapRepo.save(uniqueValuesMap);
		if (!DUPLICATE.equals(rightUniqueValue)) {
			logEntity.incrementNewDataInSync();
		}
		return true;
	}

	private boolean addUVMapping(IntegrationPropsEntity intProps, Record record, Record matchedRecord,
			String uniqueColumn, boolean isLeftToRight, SyncLogEntity logEntity) throws Exception {
		addUVMapping(intProps, record.getUniqueValue(), matchedRecord.getUniqueValue(), isLeftToRight, logEntity);
		record.setMappedRecordUniqueValue(matchedRecord.getUniqueValue());
		return true;
	}

	private boolean compareAndFillRecord(Record newRecord, Record recordA, Record recordB,
			List<FieldMapEntity> fieldMaps, boolean isLeftToRight) throws Exception {
		boolean hasAnyChange = false;
		for (FieldMapEntity fieldMap : fieldMaps) {
//			if (fieldMap.isEnabled() == false) {
//				continue;
//			}
			if (!isLeftToRight && fieldMap.isOneWay()) {
				continue;
			}
			String srcColumn;
			String srcColumnType;
			String srcColumnFormat;
			String destColumn;
			String destColumnType;
			String destColumnFormat;
			boolean isMandatory = false;
			if (isLeftToRight) {
				srcColumn = fieldMap.getLeftColumnName();
				srcColumnType = fieldMap.getLeftColumnType();
				srcColumnFormat = fieldMap.getLeftColumnFormat();

				destColumn = fieldMap.getRightColumnName();
				destColumnType = fieldMap.getRightColumnType();
				destColumnFormat = fieldMap.getRightColumnFormat();
				isMandatory = fieldMap.isRightMandatory();
			} else {
				srcColumn = fieldMap.getRightColumnName();
				srcColumnType = fieldMap.getRightColumnType();
				srcColumnFormat = fieldMap.getRightColumnFormat();

				destColumn = fieldMap.getLeftColumnName();
				destColumnType = fieldMap.getLeftColumnType();
				destColumnFormat = fieldMap.getLeftColumnFormat();
				isMandatory = fieldMap.isLeftMandatory();
			}
			if (!srcColumnType.equals(destColumnType)) {
				CommonUtil.logOsyncInfo(short_name,"Source and destination column types are not equal. Continuing for now.");
			}

			Object valueA = recordA.getValue(srcColumn);
			Object valueB = recordB == null ? null : recordB.getValue(destColumn);
			boolean isCreateOperation = recordB == null;
			if (isNull(valueA)) {
				valueA = "";
				if (isCreateOperation && isMandatory) {
					throw new OsyncException(OsyncException.Code.FIELD_MAP_MANDATORY_VALUE_MISSING,
							"Src Column: " + srcColumn + ", destColumn: " + destColumn);
				}
			}
			if (isNull(valueB)) {
				valueB = "";
			}
			if (!isEqual(valueA, valueB, destColumnType)) {
				hasAnyChange = true;
				switch (destColumnType) {
				case "boolean":
					newRecord.addOrUpdateValue(destColumn, Boolean.valueOf(valueA.toString()));
					break;
				case "number":
					try {
						newRecord.addOrUpdateValue(destColumn, Long.valueOf(valueA.toString()));
					} catch (NumberFormatException e) {
					}
					break;
				case "double":
					try {
						newRecord.addOrUpdateValue(destColumn, Double.valueOf(valueA.toString()));
					} catch (NumberFormatException e) {
					}
					break;
				case "text":
					newRecord.addOrUpdateValue(destColumn, valueA.toString());
					break;
				case "date":
				case "date_time":
					String dateValue = valueA.toString();
					if (srcColumnFormat != null && destColumnFormat != null) {
						try {
							dateValue = convertDateFormat(srcColumnFormat, destColumnFormat, valueA);
						} catch (Exception e) {
							CommonUtil.logOsyncInfo(short_name,"Exception on date formatter: " + e.getMessage()+" :: srcColumn >>"+ srcColumn+":: srcColumnFormat >>" +srcColumnFormat+ ":: srcValue >>"+ valueA+" :: destColumn >>"+destColumn +":: destColumnFormat >>"+ destColumnFormat+" ::destValue >>"+ valueB);
						}
					}
					newRecord.addOrUpdateValue(destColumn, dateValue);
					break;
				}
			}

		}
		return hasAnyChange;
	}

	private String convertDateFormat(String srcColumnFormat, String destColumnFormat, Object valueA)
			throws ParseException {
		SimpleDateFormat src = new SimpleDateFormat(srcColumnFormat);
		Date date = src.parse(valueA.toString());
		SimpleDateFormat dest = new SimpleDateFormat(destColumnFormat);
		return dest.format(date);
	}

	private boolean isEqual(Object a, Object b, String type) {
		if (a == null && b == null) {
			return true;
		}
		if (isNull(a) && isNull(b)) {
			return true;
		}
		String astr = a.toString().trim();
		String bstr = b.toString().trim();
		if (astr.equals(bstr)) {
			return true;
		}
		switch (type) {
		case "text":
			if (astr.equals(bstr)) {
				return true;
			}
			break;
		case "boolean":
			if ("true".equalsIgnoreCase(astr) && "true".equalsIgnoreCase(bstr)) {
				return true;
			}
			if ("true".equalsIgnoreCase(astr) && "1".equalsIgnoreCase(bstr)) {
				return true;
			}
			if ("1".equalsIgnoreCase(astr) && "1".equalsIgnoreCase(bstr)) {
				return true;
			}
			if ("false".equalsIgnoreCase(astr) && "false".equalsIgnoreCase(bstr)) {
				return true;
			}
			if ("false".equalsIgnoreCase(astr) && "0".equalsIgnoreCase(bstr)) {
				return true;
			}
			if ("0".equalsIgnoreCase(astr) && "false".equalsIgnoreCase(bstr)) {
				return true;
			}
			if ("1".equals(astr) && "1".equals(bstr)) {
				return true;
			}
			if ("0".equals(astr) && "0".equals(bstr)) {
				return true;
			}

		case "integer":
			if (astr.equals(bstr)) {
				return true;
			}
			break;
		}
		return false;
	}

	private boolean isNull(Object value) {
		return value == null || value.toString().trim().isEmpty() || value.toString().equalsIgnoreCase("null");
	}
}
