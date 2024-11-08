package se.centevo.endpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import org.apache.commons.io.IOUtils;
import org.hibernate.annotations.Formula;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mysema.commons.lang.URLEncoder;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@BasePathAwareController
@RequiredArgsConstructor
class ReportController {
    final private ReportRepository repository;

    @GetMapping(path = "/reports/{id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody ResponseEntity<byte[]> download(@PathVariable Long id) throws FileNotFoundException, IOException {


        var report = repository.findById(id).get();
        var in = readFileAndOutputToResponse((report.getFilePath() + "\\" + report.getFileName()).replaceAll("\\\\+", "\\\\"));
        var pdf = IOUtils.toByteArray(in);

        String filename = report.getFileName();
    	filename = URLEncoder.encodeURL(filename).replace("+"," ");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdf.length);

        return new ResponseEntity<byte[]>(pdf, headers, HttpStatus.OK);
    }
    
    public static InputStream readFileAndOutputToResponse(String url) throws FileNotFoundException, IOException{
    	
    	if(url.startsWith("file://"))
			url = url.substring("file://".length());
    	
    	File file = new File(url);
    	
    	if(!file.exists()) {
    		throw new FileNotFoundException("Could not open file.");
    	}
    	
    	FileInputStream in = new FileInputStream(url);
        return in;
    }

}

interface ReportRepository extends ReadRepository<Report, Long> {
}

@Getter @Setter
@Entity
class Report {
    @Id
    Long reportId;
    String description;
    @JsonIgnore
    String filePath;
    @JsonIgnore
    String fileName;

    @Formula("COALESCE(UpdateDate, CreateDate)")
    @LastModifiedDate LocalDateTime lastModifiedDate;
    @Version Long version;
}
